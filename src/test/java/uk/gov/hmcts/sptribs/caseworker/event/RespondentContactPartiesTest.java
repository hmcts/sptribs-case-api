package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.Field.FieldBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.event.page.RespondentPartiesToContact;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.TribunalCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_CONTACT_PARTY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.buildDynamicMultiSelectDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocumentUploadMap;

@ExtendWith(MockitoExtension.class)
class RespondentContactPartiesTest {

    @InjectMocks
    private RespondentContactParties respondentContactParties;

    @InjectMocks
    private RespondentPartiesToContact respondentPartiesToContact;

    @Mock
    private ContactPartiesNotification contactPartiesNotification;

    @Mock
    private ContactPartiesSelectDocument contactPartiesSelectDocument;

    @Mock
    private ContactPartiesService contactPartiesService;

    @Mock
    private NotificationHelper notificationHelper;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        respondentContactParties.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(RESPONDENT_CONTACT_PARTIES);
    }

    @Test
    void shouldShowOnlyDocumentsPartiesAndMessageOnReviewPage() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        respondentContactParties.configure(configBuilder);

        Event<CaseData, UserRole, State> event = getEventsFrom(configBuilder).get(RESPONDENT_CONTACT_PARTIES);
        assertThat(event.isShowSummary()).isFalse();
        assertThat(event.getFields().getPageLabels()).containsEntry("contactPartiesReview", "Check your answers");
        assertThat(event.getFields().getFields().stream()
            .map(FieldBuilder::build)
            .filter(field -> "contactPartiesReview".equals(field.getPage()))
            .map(Field::getId))
            .containsExactly("contactPartiesDocumentsD01", "contactPartiesDocumentsD02", "contactPartiesDocumentsD03",
                "contactPartiesDocumentsD04", "contactPartiesDocumentsD05", "contactPartiesDocumentsD06",
                "contactPartiesDocumentsD07", "contactPartiesDocumentsD08", "contactPartiesDocumentsD09",
                "contactPartiesDocumentsD10",
                "contactPartiesDocumentsReviewSelectedParties", "contactPartiesDocumentsReviewMessage");
        assertThat(event.getFields().getFields().stream()
            .map(FieldBuilder::build)
            .filter(field -> "contactPartiesReview".equals(field.getPage())))
            .allSatisfy(field -> assertThat(field.getContext()).isEqualTo(DisplayContext.ReadOnly));
        assertThat(event.getFields().getFields().stream()
            .map(FieldBuilder::build)
            .filter(field -> field.getId().matches("contactPartiesDocumentsD\\d{2}")))
            .allSatisfy(field -> assertThat(field.getShowCondition())
                .isEqualTo("contactPartiesDocumentsActiveDocumentSlotsCONTAINS \""
                    + field.getId().substring("contactPartiesDocuments".length()) + "\""));
        assertThat(event.getFields().getFields().stream()
            .map(FieldBuilder::build)
            .filter(field -> "contactPartiesDocumentsD01".equals(field.getId())))
            .singleElement()
            .satisfies(field -> {
                assertThat(field.getContext()).isEqualTo(DisplayContext.ReadOnly);
                assertThat(field.getPage()).isEqualTo("contactPartiesReview");
                assertThat(field.getShowCondition()).isEqualTo("contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D01\"");
                assertThat(field.getDisplayContextParameter()).isNull();
            });
    }

    @Test
    void shouldSuccessfullySaveResContactParties() {
        //Given
        final CaseData caseData = caseData();
        caseData.getContactParties().setSubjectContactParties(Set.of(SubjectCIC.SUBJECT));
        caseData.getContactParties().setApplicantContactParties(Set.of(ApplicantCIC.APPLICANT_CIC));
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .applicantContactParties(Set.of(ApplicantCIC.APPLICANT_CIC))
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .tribunal(Set.of(TribunalCIC.TRIBUNAL)).build();
        caseData.setContactParties(contactParties);

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(resContactPartiesResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyMoveToNextPage() {
        //Given
        final CaseData caseData = caseData();
        CicCase cicCase = CicCase.builder()
            .contactPartiesCIC(Set.of(ContactPartiesCIC.SUBJECTTOCONTACT))
            .fullName("Test Subject")
            .notifyPartyMessage("Review message")
            .build();
        caseData.getContactParties().setSubjectContactParties(Set.of(SubjectCIC.SUBJECT));
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(caseData.getContactPartiesDocuments().getReviewSelectedParties())
            .isEqualTo("Subject: Test Subject\nTribunal");
        assertThat(caseData.getContactPartiesDocuments().getReviewMessage()).isEqualTo("Review message");
    }

    @Test
    void shouldShowSelectedDocumentLinkOnRespondentReview() {
        CaseData caseData = caseData();
        UUID documentId = UUID.randomUUID();
        Document document = Document.builder()
            .url("https://example.test/documents/" + documentId).filename("selected.pdf").build();
        ListValue<CaseworkerCICDocument> documentValue = new ListValue<>();
        documentValue.setValue(CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS).documentLink(document).build());
        caseData.getCicCase().setReinstateDocuments(List.of(documentValue));
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        DynamicListElement selection = DynamicListElement.builder().code(documentId)
            .label("[selected.pdf](https://example.test/documents/" + documentId + "/binary)").build();
        caseData.getContactPartiesDocuments().setDocumentList(DynamicMultiSelectList.builder()
            .value(List.of(selection)).listItems(List.of(selection)).build());
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        assertThat(respondentPartiesToContact.midEvent(details, details).getErrors()).isEmpty();
        assertThat(caseData.getContactPartiesDocuments().getD01()).isEqualTo(documentValue.getValue());
        assertThat(caseData.getContactPartiesDocuments().getD02()).isNull();
    }

    @Test
    void shouldNotSuccessfullyMoveToNextPageWithError() {
        //Given
        Set<SubjectCIC> sub = new HashSet<>();
        Set<ApplicantCIC> app = new HashSet<>();
        Set<RepresentativeCIC> rep = new HashSet<>();
        Set<TribunalCIC> tri = new HashSet<>();

        ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(sub)
            .applicantContactParties(app)
            .representativeContactParties(rep)
            .tribunal(tri).build();
        final CaseData caseData = caseData();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(caseData.getContactParties().getSubjectContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getApplicantContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getTribunal()).isEmpty();
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(SELECT_AT_LEAST_ONE_CONTACT_PARTY);

        //When
        SubmittedCallbackResponse contactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Applicant");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Tribunal");
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparationAndInsertToDocCorrespondence() {
        //Given
        DynamicMultiSelectList documentList = buildDynamicMultiSelectDocumentList();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(documentList)
            .build();
        Set<SubjectCIC> sub = new HashSet<>();
        sub.add(SubjectCIC.SUBJECT);
        Set<ApplicantCIC> app = new HashSet<>();
        app.add(ApplicantCIC.APPLICANT_CIC);
        Set<RepresentativeCIC> rep = new HashSet<>();
        rep.add(RepresentativeCIC.REPRESENTATIVE);
        Set<TribunalCIC> tri = new HashSet<>();
        tri.add(TribunalCIC.TRIBUNAL);

        ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(sub)
            .applicantContactParties(app)
            .representativeContactParties(rep)
            .tribunal(tri).build();
        final CaseData caseData = caseData();
        caseData.setContactParties(contactParties);
        caseData.setContactPartiesDocuments(contactPartiesDocuments);
        caseData.setHyphenatedCaseRef(String.valueOf(TEST_CASE_ID));

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        final int docAttachLimit = 10;
        Map<String, String> emailDocs = getDocumentUploadMap();

        when(notificationHelper.buildDocumentList(documentList, docAttachLimit)).thenReturn(emailDocs);
        when(contactPartiesNotification.sendToSubject(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID1");
        when(contactPartiesNotification.sendToRepresentative(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID2");
        when(contactPartiesNotification.sendToApplicant(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID3");
        when(contactPartiesNotification.sendToTribunal(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID4");

        //When
        SubmittedCallbackResponse response =
            respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(caseData.getContactParties().getSubjectContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getApplicantContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getTribunal()).hasSize(1);
        assertThat(response).isNotNull();

        //When
        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(resContactPartiesResponse).isNotNull();
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Applicant");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Tribunal");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains(",");

        verify(contactPartiesService, times(2)).linkCorrespondenceIdsToDocuments(caseData, emailDocs,
            List.of("UUID1", "UUID2", "UUID3", "UUID4"));
    }

    @Test
    void shouldNotCallDocumentCorrespondenceServiceAsNoEmailsSent() {
        //Given
        DynamicMultiSelectList documentList = buildDynamicMultiSelectDocumentList();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(documentList)
            .build();

        final CaseData caseData = caseData();
        caseData.setContactPartiesDocuments(contactPartiesDocuments);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        final int docAttachLimit = 10;
        Map<String, String> emailDocs = getDocumentUploadMap();

        when(notificationHelper.buildDocumentList(documentList, docAttachLimit)).thenReturn(emailDocs);

        //When
        SubmittedCallbackResponse response =
            respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();

        //When
        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(resContactPartiesResponse).isNotNull();

        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldSuccessfullyMoveToNextPageWithOutError() {
        //Given
        final CaseData caseData = caseData();
        CicCase cicCase = CicCase.builder().contactPartiesCIC(Set.of()).build();
        cicCase.setRepresentativeFullName("www");
        caseData.setCicCase(cicCase);
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldClearPreviewDocumentsBeforeSubmit() {
        final CaseData caseData = caseData();
        caseData.getContactPartiesDocuments().setD01(CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("selected.pdf").build()).build());
        caseData.getContactPartiesDocuments().setReviewSelectedParties("Tribunal");
        caseData.getContactPartiesDocuments().setReviewMessage("Review message");
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(State.Draft);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentContactParties.aboutToSubmit(details, details);

        assertThat(response.getData()).isSameAs(caseData);
        assertThat(response.getState()).isEqualTo(State.Draft);
        assertThat(caseData.getContactPartiesDocuments().getD01()).isNull();
        assertThat(caseData.getContactPartiesDocuments().getReviewSelectedParties()).isNull();
        assertThat(caseData.getContactPartiesDocuments().getReviewMessage()).isNull();
    }

    @Test
    void shouldSendMessageWithoutAttachmentsWhenDocumentListIsMissing() {
        CaseData caseData = caseData();
        caseData.setCicCase(CicCase.builder().build());
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        caseData.getContactPartiesDocuments().setDocumentList(null);
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = respondentContactParties.aboutToSubmit(details, details);

        assertThat(response.getData()).isSameAs(caseData);
        DynamicMultiSelectList documentList = caseData.getContactPartiesDocuments().getDocumentList();
        assertThat(documentList.getValue()).isEmpty();

        Map<String, String> emptyDocuments = new NotificationHelper().buildDocumentList(documentList, 10);
        when(notificationHelper.buildDocumentList(documentList, 10)).thenReturn(emptyDocuments);
        when(contactPartiesNotification.sendToTribunal(caseData, caseData.getHyphenatedCaseRef(), emptyDocuments))
            .thenReturn("correspondence-id");

        SubmittedCallbackResponse submitted = respondentContactParties.submitted(details, details);

        assertThat(submitted.getConfirmationHeader()).contains("# Message sent");
        verify(contactPartiesNotification).sendToTribunal(caseData, caseData.getHyphenatedCaseRef(), emptyDocuments);
    }

}
