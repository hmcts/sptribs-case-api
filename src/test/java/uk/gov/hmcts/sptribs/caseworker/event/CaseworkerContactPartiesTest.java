package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesReview;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesSelectDocument;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.common.event.page.PartiesToContact;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_CONTACT_PARTY;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CONTACT_PARTIES;

@ExtendWith(MockitoExtension.class)
class CaseworkerContactPartiesTest {
    @InjectMocks
    private CaseworkerContactParties caseWorkerContactParties;

    @InjectMocks
    private PartiesToContact partiesToContact;

    @Mock
    private ContactPartiesNotification contactPartiesNotification;

    @Mock
    private ContactPartiesSelectDocument contactPartiesSelectDocument;

    @Mock
    private ContactPartiesReview contactPartiesReview;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseWorkerContactParties.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CONTACT_PARTIES);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullyPrepareDocumentListInAboutToStartCallback() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        final CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerContactParties.aboutToStart(caseDetails);

        assertThat(response.getData().getContactPartiesDocuments().getDocumentList()).isNotNull();
        assertThat(response.getData().getContactPartiesDocuments().getDocumentList().getListItems()).hasSize(1);
        assertThat(response.getData().getContactPartiesDocuments().getPreviewDoc()).hasSize(1);
    }

    @Test
    void shouldKeepPreviewDocumentsInSyncWithContactPartiesDocumentList() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();

        final CaseworkerCICDocument validDocument = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("valid-url").binaryUrl("valid-url").filename("valid.pdf").build())
            .build();

        final CaseworkerCICDocument invalidDocument = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("invalid-url").binaryUrl("invalid-url").filename("invalid.exe").build())
            .build();

        ListValue<CaseworkerCICDocument> validListValue = new ListValue<>();
        validListValue.setValue(validDocument);
        listValueList.add(validListValue);

        ListValue<CaseworkerCICDocument> invalidListValue = new ListValue<>();
        invalidListValue.setValue(invalidDocument);
        listValueList.add(invalidListValue);

        final CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerContactParties.aboutToStart(caseDetails);

        assertThat(response.getData().getContactPartiesDocuments().getDocumentList()).isNotNull();
        assertThat(response.getData().getContactPartiesDocuments().getDocumentList().getListItems()).hasSize(1);
        assertThat(response.getData().getContactPartiesDocuments().getPreviewDoc()).hasSize(1);
        assertThat(response.getData().getContactPartiesDocuments().getPreviewDoc().get(0).getValue().getDocumentLink().getFilename())
            .isEqualTo("valid.pdf");
    }

    @Test
    void shouldSuccessfullyMoveToNextPage() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }


    @Test
    void shouldNotSuccessfullyMoveToNextPageWithError() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(SELECT_AT_LEAST_ONE_CONTACT_PARTY);

        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Respondent");
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparation() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        SubmittedCallbackResponse response =
            caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);

        assertThat(caseData.getCicCase().getNotifyPartySubject()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyApplicant()).hasSize(1);
        assertThat(response).isNotNull();

        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);

        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparationIfSubjectIsNull() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        Mockito.doNothing().when(contactPartiesNotification).sendToApplicant(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(contactPartiesNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(contactPartiesNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());

        SubmittedCallbackResponse response =
            caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyApplicant()).hasSize(1);
        assertThat(response).isNotNull();

        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);

        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Applicant");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");
    }

    @Test
    void shouldDisplayTheCorrectFailureMessageIfExceptionThrownByNotification() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        doThrow(NotificationException.class)
            .when(contactPartiesNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());

        SubmittedCallbackResponse response =
            caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);

        assertThat(response.getConfirmationHeader()).contains("Contact Parties notification failed");
        assertThat(response.getConfirmationHeader()).contains("Please resend the notification");
    }

    @Test
    void shouldSuccessfullyMoveToNextPageWithOutError() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldRunAboutToStart() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();

        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerContactParties.aboutToStart(updatedCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getContactPartiesDocuments().getDocumentList().getListItems()).hasSize(1);
        assertThat(response.getData().getCicCase().getNotifyPartyMessage()).isEqualTo("");
    }

    @Test
    void shouldBuildContactPartiesReviewInAboutToSubmit() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        List<ListValue<CaseworkerCICDocument>> selectedDocuments = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        selectedDocuments.add(list);

        CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartyMessage("message")
            .build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseData.getContactPartiesDocuments().setPreviewDoc(selectedDocuments);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerContactParties.aboutToSubmit(updatedCaseDetails, beforeDetails);

        uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesReview review = response.getData().getContactPartiesReview();
        assertThat(review).isNotNull();
        assertThat(review.getPreviewDoc()).hasSize(1);
        assertThat(review.getContactParties().getSubjectContactParties()).contains(SubjectCIC.SUBJECT);
        assertThat(review.getContactParties().getRepresentativeContactParties()).contains(RepresentativeCIC.REPRESENTATIVE);
        assertThat(review.getContactParties().getApplicantContactParties()).contains(ApplicantCIC.APPLICANT_CIC);
        assertThat(review.getContactParties().getRespondent()).contains(RespondentCIC.RESPONDENT);
        assertThat(review.getContactParties().getMessage()).isEqualTo("message");
        assertThat(review.getMessage()).isEqualTo("message");
    }

    @Test
    void shouldBuildContactPartiesReviewWithNoRecipientsInAboutToSubmit() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().notifyPartyMessage("message only").build())
            .build();
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerContactParties.aboutToSubmit(updatedCaseDetails, beforeDetails);

        uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesReview review = response.getData().getContactPartiesReview();
        assertThat(review).isNotNull();
        assertThat(review.getPreviewDoc()).isNull();
        assertThat(review.getContactParties()).isNotNull();
        assertThat(review.getContactParties().getSubjectContactParties()).isNull();
        assertThat(review.getContactParties().getRepresentativeContactParties()).isNull();
        assertThat(review.getContactParties().getApplicantContactParties()).isNull();
        assertThat(review.getContactParties().getRespondent()).isNull();
        assertThat(review.getContactParties().getMessage()).isEqualTo("message only");
        assertThat(review.getMessage()).isEqualTo("message only");
    }
}
