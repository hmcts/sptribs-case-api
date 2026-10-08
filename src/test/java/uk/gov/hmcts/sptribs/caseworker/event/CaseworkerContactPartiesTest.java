package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
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
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.CONTACT_PARTIES_NOTIFICATION_FAILED;
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
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.buildDynamicMultiSelectDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocumentUploadMap;
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
    private ContactPartiesService contactPartiesService;

    @Mock
    private NotificationHelper notificationHelper;

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
    void shouldShowOnlyDocumentsPartiesAndMessageOnReviewPage() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        ReflectionTestUtils.setField(caseWorkerContactParties, "contactPartiesSelectDocument",
            new ContactPartiesSelectDocument(null, null, null));

        caseWorkerContactParties.configure(configBuilder);

        Event<CaseData, UserRole, State> event = getEventsFrom(configBuilder).get(CASEWORKER_CONTACT_PARTIES);
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
            .filter(field -> "contactPartiesDocumentsActiveDocumentSlots".equals(field.getId())))
            .singleElement()
            .satisfies(field -> {
                assertThat(field.getPage()).isEqualTo("contactPartiesSelectDocument");
                assertThat(field.getShowCondition()).isEqualTo("[STATE]=\"ALWAYS_HIDE\"");
            });
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
    void shouldSuccessfullyPrepareDocumentListInAboutToStartCallback() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        final CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> docListValue = new ListValue<>();
        docListValue.setValue(doc);
        listValueList.add(docListValue);

        String orderFilename = "Order--[Subject kaikaqsrf]--14-07-2026 15:39:57.pdf";
        String orderDocUrlUUID = UUID.randomUUID().toString();

        final CaseworkerCICDocument orderDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .documentLink(Document.builder()
                .url("http://mocked-url.com/documents/" + orderDocUrlUUID)
                .binaryUrl("http://mocked-url.com/documents/" + orderDocUrlUUID + "/binary")
                .filename(orderFilename)
                .build())
            .build();
        ListValue<CaseworkerCICDocument> orderDocListValue = new ListValue<>();
        orderDocListValue.setValue(orderDoc);
        listValueList.add(orderDocListValue);

        final CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        ReflectionTestUtils.setField(caseWorkerContactParties, "baseUrl", "http://mocked-url.com/");

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerContactParties.aboutToStart(caseDetails);

        assertThat(response.getData().getContactPartiesDocuments().getDocumentList()).isNotNull();
        assertThat(response.getData().getContactPartiesDocuments().getDocumentList().getListItems()).hasSize(2);

        String expectedSelectedDoc = "[" + orderFilename + " " + orderDoc.getDocumentCategory().getLabel() + "]"
            + "(http://mocked-url.com/documents/" + orderDocUrlUUID + "/binary)";
        DynamicListElement responseOrderDoc = new DynamicListElement();

        for (DynamicListElement responseDoc : response.getData().getContactPartiesDocuments().getDocumentList().getListItems()) {
            if (responseDoc.getLabel().contains(orderFilename)) {
                responseOrderDoc =  responseDoc;
            }
        }

        assertThat(responseOrderDoc.getLabel()).isEqualTo(expectedSelectedDoc);
    }

    @Test
    void shouldSuccessfullyMoveToNextPage() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyMessage("Review message")
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
        assertThat(caseData.getContactPartiesDocuments().getReviewSelectedParties())
            .isEqualTo("Subject: " + TEST_FIRST_NAME + "\nRepresentative: " + TEST_SOLICITOR_NAME);
        assertThat(caseData.getContactPartiesDocuments().getReviewMessage()).isEqualTo("Review message");
    }

    @Test
    void shouldRejectMissingCaseDetailsWithoutResolvingSelectedDocuments() {
        CaseData caseData = caseData();
        caseData.setCicCase(null);
        ContactPartiesDocuments documents = caseData.getContactPartiesDocuments();
        documents.setDocumentList(DynamicMultiSelectList.builder()
            .value(List.of(DynamicListElement.builder().code(UUID.randomUUID()).build()))
            .build());
        documents.setReviewSelectedParties("Previous selection");
        documents.setReviewMessage("Previous message");
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = partiesToContact.midEvent(details, details);

        assertThat(response.getErrors()).containsExactly(CONTACT_PARTIES_NOTIFICATION_FAILED);
        assertThat(documents.getReviewSelectedParties()).isNull();
        assertThat(documents.getReviewMessage()).isNull();
    }

    @Test
    void shouldShowSelectedDocumentLinksAndClearLinksRemovedOnReturnToReview() {
        CaseData caseData = caseData();
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Document first = Document.builder().url("https://example.test/documents/" + firstId)
            .filename("first.pdf").build();
        Document second = Document.builder().url("https://example.test/documents/" + secondId)
            .filename("second.pdf").build();
        ListValue<CaseworkerCICDocument> firstValue = new ListValue<>();
        firstValue.setValue(CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS).documentLink(first).build());
        ListValue<CaseworkerCICDocument> secondValue = new ListValue<>();
        secondValue.setValue(CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS).documentLink(second).build());
        caseData.getCicCase().setReinstateDocuments(List.of(firstValue, secondValue));
        caseData.getCicCase().setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        DynamicListElement firstSelection = DynamicListElement.builder().code(firstId)
            .label("[first.pdf](https://example.test/documents/" + firstId + "/binary)").build();
        DynamicListElement secondSelection = DynamicListElement.builder().code(secondId)
            .label("[second.pdf](https://example.test/documents/" + secondId + "/binary)").build();
        caseData.getContactPartiesDocuments().setDocumentList(DynamicMultiSelectList.builder()
            .value(List.of(firstSelection, secondSelection))
            .listItems(List.of(firstSelection, secondSelection)).build());
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        assertThat(partiesToContact.midEvent(details, details).getErrors()).isEmpty();
        assertThat(caseData.getContactPartiesDocuments().getD01()).isEqualTo(firstValue.getValue());
        assertThat(caseData.getContactPartiesDocuments().getD02()).isEqualTo(secondValue.getValue());
        assertThat(caseData.getContactPartiesDocuments().getD03()).isNull();

        caseData.getContactPartiesDocuments().getDocumentList().setValue(List.of(secondSelection));
        assertThat(partiesToContact.midEvent(details, details).getErrors()).isEmpty();
        assertThat(caseData.getContactPartiesDocuments().getD01()).isEqualTo(secondValue.getValue());
        assertThat(caseData.getContactPartiesDocuments().getD02()).isNull();
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
    void shouldDisplayTheCorrectMessageWithCommaSeparationAndLinkDocumentsToCorrespondence() {
        //given
        DynamicMultiSelectList documentList = buildDynamicMultiSelectDocumentList();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(documentList)
            .build();

        final CaseData caseData = caseData();
        caseData.setContactPartiesDocuments(contactPartiesDocuments);
        caseData.setHyphenatedCaseRef(String.valueOf(TEST_CASE_ID));

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

        final int docAttachLimit = 10;
        Map<String, String> emailDocs = getDocumentUploadMap();

        when(notificationHelper.buildDocumentList(documentList, docAttachLimit)).thenReturn(emailDocs);
        when(contactPartiesNotification.sendToSubject(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID1");
        when(contactPartiesNotification.sendToRepresentative(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID2");
        when(contactPartiesNotification.sendToApplicant(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID3");
        when(contactPartiesNotification.sendToRespondent(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID4");

        SubmittedCallbackResponse contactPartiesResponse =
            caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);

        assertThat(caseData.getCicCase().getNotifyPartySubject()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyApplicant()).hasSize(1);

        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");

        verify(contactPartiesService, times(1)).linkCorrespondenceIdsToDocuments(caseData, emailDocs,
            List.of("UUID1", "UUID2", "UUID3", "UUID4"));
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparationIfSubjectIsNullAndLinkDocumentsToCorrespondence() {
        //given
        DynamicMultiSelectList documentList = buildDynamicMultiSelectDocumentList();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(documentList)
            .build();

        final CaseData caseData = caseData();
        caseData.setContactPartiesDocuments(contactPartiesDocuments);
        caseData.setHyphenatedCaseRef(String.valueOf(TEST_CASE_ID));

        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final int docAttachLimit = 10;
        Map<String, String> emailDocs = getDocumentUploadMap();

        when(notificationHelper.buildDocumentList(documentList, docAttachLimit)).thenReturn(emailDocs);
        when(contactPartiesNotification.sendToRepresentative(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID2");
        when(contactPartiesNotification.sendToApplicant(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID3");
        when(contactPartiesNotification.sendToRespondent(caseData, String.valueOf(TEST_CASE_ID), emailDocs)).thenReturn("UUID4");

        SubmittedCallbackResponse contactPartiesResponse =
            caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyApplicant()).hasSize(1);

        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Applicant");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");

        verify(contactPartiesNotification, never()).sendToSubject(any(), any(), any());
        verify(contactPartiesService, times(1)).linkCorrespondenceIdsToDocuments(caseData, emailDocs, List.of("UUID2", "UUID3", "UUID4"));
    }

    @Test
    void shouldNotCallDocumentCorrespondenceServiceAsNoEmailsSent() {

        //given
        DynamicMultiSelectList documentList = buildDynamicMultiSelectDocumentList();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(documentList)
            .build();

        final CaseData caseData = caseData();
        caseData.setContactPartiesDocuments(contactPartiesDocuments);

        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .representativeAddress(SOLICITOR_ADDRESS)
            .build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final int docAttachLimit = 10;
        Map<String, String> emailDocs = getDocumentUploadMap();

        when(notificationHelper.buildDocumentList(documentList, docAttachLimit)).thenReturn(emailDocs);

        //when
        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.submitted(updatedCaseDetails, beforeDetails);

        //then
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Applicant");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Respondent");

        verifyNoInteractions(contactPartiesService);

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
        caseData.getContactPartiesDocuments().setD10(CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("stale.pdf").build()).build());
        updatedCaseDetails.setData(caseData);

        ReflectionTestUtils.setField(caseWorkerContactParties, "baseUrl", "http://mocked-url.com/");

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerContactParties.aboutToStart(updatedCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getContactPartiesDocuments().getDocumentList().getListItems()).hasSize(1);
        assertThat(response.getData().getContactPartiesDocuments().getD01()).isNull();
        assertThat(response.getData().getContactPartiesDocuments().getD10()).isNull();
        assertThat(response.getData().getCicCase().getNotifyPartyMessage()).isEqualTo("");
    }

    @Test
    void shouldPopulateEventMetaDataForSummaryAndDescription() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .build();

        caseData.setCicCase(cicCase);

        ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
        List<DynamicListElement> selection = List.of(DynamicListElement.builder()
                .code(UUID.randomUUID())
                .label("[Document 1 - Test.pdf][https://manage-cases.hmcts.net/test123")
                .build());
        contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
            .value(selection)
            .listItems(selection)
            .build());
        contactPartiesDocuments.setReviewSelectedParties("Subject: Test Subject");
        contactPartiesDocuments.setReviewMessage("Review message");

        caseData.setContactPartiesDocuments(contactPartiesDocuments);
        caseData.getContactPartiesDocuments().setD10(CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("selected.pdf").build()).build());

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> contactPartiesResponse = caseWorkerContactParties
            .aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(contactPartiesResponse.getEventMetadata().getSummary()).isEqualTo("1 Selected documents sent");
        assertThat(contactPartiesResponse.getEventMetadata().getDescription()).contains("Document 1 - Test.pdf");
        assertThat(caseData.getContactPartiesDocuments().getD01()).isNull();
        assertThat(caseData.getContactPartiesDocuments().getD10()).isNull();
        assertThat(caseData.getContactPartiesDocuments().getReviewSelectedParties()).isNull();
        assertThat(caseData.getContactPartiesDocuments().getReviewMessage()).isNull();
    }

    @Test
    void shouldSendMessageWithoutAttachmentsWhenSelectionIsNull() {
        CaseData caseData = caseData();
        caseData.setCicCase(CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT)).build());
        DynamicMultiSelectList documentList = DynamicMultiSelectList.builder().build();
        caseData.getContactPartiesDocuments().setDocumentList(documentList);
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerContactParties.aboutToSubmit(details, details);

        assertThat(response.getEventMetadata().getSummary()).isEqualTo("0 Selected documents sent");
        assertThat(documentList.getValue()).isEmpty();

        Map<String, String> emptyDocuments = new NotificationHelper().buildDocumentList(documentList, 10);
        when(notificationHelper.buildDocumentList(documentList, 10)).thenReturn(emptyDocuments);
        when(contactPartiesNotification.sendToSubject(caseData, caseData.getHyphenatedCaseRef(), emptyDocuments))
            .thenReturn("correspondence-id");

        SubmittedCallbackResponse submitted = caseWorkerContactParties.submitted(details, details);

        assertThat(submitted.getConfirmationHeader()).contains("# Message sent");
        verify(contactPartiesNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef(), emptyDocuments);
    }

}
