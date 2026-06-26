package uk.gov.hmcts.sptribs.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssMessage;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.dispatcher.DssUpdateCaseSubmissionNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.DSS_TRIBUNAL_FORM;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDssCaseData;

@ExtendWith(MockitoExtension.class)
class CicDssUpdateCaseEventTest {

    @Mock
    private DssUpdateCaseSubmissionNotification dssUpdateCaseSubmissionNotification;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private DocumentsService documentsService;

    @InjectMocks
    private CicDssUpdateCaseEvent cicDssUpdateCaseEvent;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        cicDssUpdateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);

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
    void shouldAddDocumentsAndMessagesToCaseDataInAboutToSubmitCallback() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final DssMessage message = DssMessage.builder()
            .message("new doc")
            .build();
        final List<ListValue<DssMessage>> messages = new ArrayList<>();
        messages.add(new ListValue<>("1", message));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .messages(messages)
            .dssCaseData(getDssCaseData())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());
        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(2);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();

        verify(documentsService, times(2)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
        Document expectedDoc1 = getDssCaseData().getOtherInfoDocuments().getFirst().getValue().getDocumentLink();
        expectedDoc1.setCategoryId("DSS");
        Document expectedDoc2 = getDssCaseData().getOtherInfoDocuments().get(1).getValue().getDocumentLink();
        expectedDoc2.setCategoryId("DSS");
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc1), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER),  eq(CaseDocumentType.APPLICANT)
        );
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc2), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER),  eq(CaseDocumentType.APPLICANT)
        );
    }

    @Test
    void shouldAddDocumentsOnlyToCaseDataInAboutToSubmitCallbackIfAdditionalInfoFieldIsEmpty() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final DssMessage message = DssMessage.builder()
            .message("new doc")
            .build();
        final List<ListValue<DssMessage>> messages = new ArrayList<>();
        messages.add(new ListValue<>("1", message));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .messages(messages)
            .dssCaseData(getDssCaseData())
            .build();
        caseData.getDssCaseData().setAdditionalInformation("");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(1);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();

        verify(documentsService, times(2)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
        Document expectedDoc1 = getDssCaseData().getOtherInfoDocuments().getFirst().getValue().getDocumentLink();
        expectedDoc1.setCategoryId("DSS");
        Document expectedDoc2 = getDssCaseData().getOtherInfoDocuments().get(1).getValue().getDocumentLink();
        expectedDoc2.setCategoryId("DSS");
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc1), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc2), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
    }

    @Test
    void shouldAddDocumentsOnlyToCaseDataInAboutToSubmitCallbackIfAdditionalInfoFieldIsNull() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final DssMessage message = DssMessage.builder()
            .message("new doc")
            .build();
        final List<ListValue<DssMessage>> messages = new ArrayList<>();
        messages.add(new ListValue<>("1", message));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .messages(messages)
            .dssCaseData(getDssCaseData())
            .build();
        caseData.getDssCaseData().setAdditionalInformation(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(1);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();

        verify(documentsService, times(2)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER),  eq(CaseDocumentType.APPLICANT)
        );
        Document expectedDoc1 = getDssCaseData().getOtherInfoDocuments().getFirst().getValue().getDocumentLink();
        expectedDoc1.setCategoryId("DSS");
        Document expectedDoc2 = getDssCaseData().getOtherInfoDocuments().get(1).getValue().getDocumentLink();
        expectedDoc2.setCategoryId("DSS");
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc1), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc2), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
    }

    @Test
    void shouldCreateNewApplicantDocumentsUploadedAndMessagesListIfEmptyInAboutToSubmitCallback() {
        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(new ArrayList<>())
                    .build()
            )
            .messages(new ArrayList<>())
            .dssCaseData(getDssCaseData())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        verify(documentsService, times(2)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(2);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(1);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();


        Document expectedDoc1 = getDssCaseData().getOtherInfoDocuments().getFirst().getValue().getDocumentLink();
        expectedDoc1.setCategoryId("DSS");
        Document expectedDoc2 = getDssCaseData().getOtherInfoDocuments().get(1).getValue().getDocumentLink();
        expectedDoc2.setCategoryId("DSS");
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc1), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(expectedDoc2), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT)
        );
    }

    @Test
    void shouldSendEmailNotifications() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        SubmittedCallbackResponse response = cicDssUpdateCaseEvent.submitted(details, details);

        assertThat(response.getConfirmationHeader())
            .isEqualTo("# CIC Dss Update Case Event Email notifications sent");

        verify(dssUpdateCaseSubmissionNotification).sendToApplicant(
            details.getData(),
            TEST_CASE_ID.toString()
        );
        verify(dssUpdateCaseSubmissionNotification).sendToTribunal(
            details.getData(),
            TEST_CASE_ID.toString()
        );
    }

    @Test
    void shouldCatchErrorIfSendEmailNotificationFails() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        doThrow(NotificationException.class)
            .when(dssUpdateCaseSubmissionNotification)
            .sendToApplicant(details.getData(), TEST_CASE_ID.toString());

        SubmittedCallbackResponse response = cicDssUpdateCaseEvent.submitted(details, details);

        assertThat(response.getConfirmationHeader())
            .contains("# CIC Dss Update Case Event Email notification failed %n## Please resend the notification");
    }

    @Test
    void shouldStoreErrorsWhenBuildAndSaveNewDocumentEntityThrowsRuntimeException() {
        final Document genericTestDocument = Document.builder().filename("doc1.pdf").build();
        final CitizenCICDocument dssOtherInfoDoc = new CitizenCICDocument();
        dssOtherInfoDoc.setDocumentLink(genericTestDocument);
        final ListValue<CitizenCICDocument> otherInfoDocListValue = new ListValue<>();
        otherInfoDocListValue.setValue(dssOtherInfoDoc);

        final DssCaseData dssCaseData = DssCaseData.builder()
            .otherInfoDocuments(List.of(otherInfoDocListValue))
            .build();

        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .dssCaseData(dssCaseData)
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        doThrow(new RuntimeException("Error saving document entity to database"))
            .when(documentsService).buildAndSaveNewDocumentEntity(any(), eq(TEST_CASE_ID),
                eq(DocumentType.DSS_OTHER),  eq(CaseDocumentType.APPLICANT));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Error saving document with filename: "
            + getDssCaseData().getOtherInfoDocuments().getFirst().getValue().getDocumentLink().getFilename());

        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER),  eq(CaseDocumentType.APPLICANT)
        );

        genericTestDocument.setFilename(null);
        dssOtherInfoDoc.setDocumentLink(genericTestDocument);
        otherInfoDocListValue.setValue(otherInfoDocListValue.getValue());
        dssCaseData.setOtherInfoDocuments(List.of(otherInfoDocListValue));
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> nullFilenameResponse =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(nullFilenameResponse.getErrors()).hasSize(1);
        assertThat(nullFilenameResponse.getErrors()).contains("Error saving document with no filename");

        genericTestDocument.setFilename("");
        dssOtherInfoDoc.setDocumentLink(genericTestDocument);
        otherInfoDocListValue.setValue(otherInfoDocListValue.getValue());
        dssCaseData.setOtherInfoDocuments(List.of(otherInfoDocListValue));
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> emptyFilenameResponse =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(emptyFilenameResponse.getErrors()).hasSize(1);
        assertThat(emptyFilenameResponse.getErrors()).contains("Error saving document with no filename");
    }

    @Test
    void shouldStoreErrorsWhenBuildAndSaveNewDocumentEntityThrowsRuntimeExceptionForMultipleDocuments() {
        final Document testHappyDocument = Document.builder().filename("happy_file.pdf").build();
        final Document testUnhappyDocument = Document.builder().filename("unhappy_file.pdf").build();

        List<ListValue<CitizenCICDocument>> otherInfoDocs = new ArrayList<>();

        final CitizenCICDocument dssOtherInfoDoc1 = new CitizenCICDocument();
        dssOtherInfoDoc1.setDocumentLink(testHappyDocument);
        final ListValue<CitizenCICDocument> otherInfoDocListValue1 = new ListValue<>();
        otherInfoDocListValue1.setValue(dssOtherInfoDoc1);
        otherInfoDocs.add(otherInfoDocListValue1);

        final CitizenCICDocument dssOtherInfoDoc2 = new CitizenCICDocument();
        dssOtherInfoDoc2.setDocumentLink(Document.builder().filename("").build());
        final ListValue<CitizenCICDocument> otherInfoDocListValue2 = new ListValue<>();
        otherInfoDocListValue2.setValue(dssOtherInfoDoc2);
        otherInfoDocs.add(otherInfoDocListValue2);

        final CitizenCICDocument dssOtherInfoDoc3 = new CitizenCICDocument();
        dssOtherInfoDoc3.setDocumentLink(testUnhappyDocument);
        final ListValue<CitizenCICDocument> otherInfoDocListValue3 = new ListValue<>();
        otherInfoDocListValue3.setValue(dssOtherInfoDoc3);
        otherInfoDocs.add(otherInfoDocListValue3);

        final DssCaseData dssCaseData = DssCaseData.builder()
            .otherInfoDocuments(otherInfoDocs)
            .build();

        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .dssCaseData(dssCaseData)
            .build();

        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        updatedCaseDetails.setData(caseData);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        doThrow(new RuntimeException("Error saving document entity to database"))
            .when(documentsService).buildAndSaveNewDocumentEntity(
                argThat(doc -> "unhappy_file.pdf".equals(doc.getFilename())
                    || "".equals(doc.getFilename())),
                eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT));

        doNothing().when(documentsService).buildAndSaveNewDocumentEntity(
            argThat(doc -> "happy_file.pdf".equals(doc.getFilename())),
            eq(TEST_CASE_ID), eq(DocumentType.DSS_OTHER), eq(CaseDocumentType.APPLICANT));

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains("Error saving document with filename: " + testUnhappyDocument.getFilename());
        assertThat(response.getErrors()).contains("Error saving document with no filename");
    }

}
