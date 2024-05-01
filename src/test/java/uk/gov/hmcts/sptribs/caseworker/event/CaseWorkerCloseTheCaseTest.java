package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.CaseWithdrawnNotification;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SUBJECT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.closedCaseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CLOSE_THE_CASE;

@ExtendWith(MockitoExtension.class)
class CaseWorkerCloseTheCaseTest {

    @InjectMocks
    private CaseworkerCloseTheCase caseworkerCloseTheCase;

    @Mock
    private JudicialService judicialService;

    @Mock
    private CaseWithdrawnNotification caseWithdrawnNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCloseTheCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CLOSE_THE_CASE);
    }

    @Test
    void shouldRunAboutToStart() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        final DynamicList userList = new DynamicList();
        when(judicialService.getAllUsers(caseData)).thenReturn(userList);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCloseTheCase.aboutToStart(updatedCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCloseCase().getRejectionName()).isEqualTo(userList);
        assertThat(response.getData().getCloseCase().getStrikeOutName()).isEqualTo(userList);
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_CLOSE_THE_CASE);
    }

    @Test
    void shouldSuccessfullyChangeCaseManagementStateToClosedState() {
        final CaseData caseData = closedCaseData();
        final CaseworkerCICDocumentUpload document = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder().build())
            .documentEmailContent("some email content")
            .documentCategory(DocumentType.LINKED_DOCS)
            .build();
        final List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        final ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);

        final CloseCase closeCase = CloseCase.builder().documentsUpload(documentList).build();
        caseData.setCloseCase(closeCase);

        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .respondentEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();

        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        assertThat(caseData.getCaseStatus()).isEqualTo(State.CaseManagement);
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCloseTheCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        final SubmittedCallbackResponse closedCase =
            caseworkerCloseTheCase.submitted(updatedCaseDetails, beforeDetails);

        assertThat(closedCase).isNotNull();
        assertThat(closedCase.getConfirmationHeader()).contains("Case closed");
        assertThat(response.getState()).isEqualTo(State.CaseClosed);
    }

    @Test
    void shouldReturnErrorForInvalidUploadedDocument() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CloseCase closeCase = CloseCase.builder().documentsUpload(getCaseworkerCICDocumentUploadList("file.xml")).build();
        final CaseData caseData = CaseData.builder()
            .closeCase(closeCase)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCloseTheCase.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventShouldValidateUploadedDocumentsOnce() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CloseCase closeCase = CloseCase.builder().documentsUpload(getCaseworkerCICDocumentUploadList("file.xml")).build();
        final CaseData caseData = CaseData.builder()
            .closeCase(closeCase)
            .build();
        caseDetails.setData(caseData);
        try (MockedStatic<DocumentUtil> mockedDocumentUtils = Mockito.mockStatic(DocumentUtil.class)) {
            mockedDocumentUtils.when(() -> DocumentUtil.validateUploadedDocuments(anyList()))
                .thenReturn(Collections.emptyList());

            caseworkerCloseTheCase.midEvent(caseDetails, caseDetails);

            mockedDocumentUtils.verify(() ->  DocumentUtil.validateUploadedDocuments(anyList()), times(1));
        }
    }

}
