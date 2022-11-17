package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseDocumentsCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.ReinstateCase.CASEWORKER_REINSTATE_CASE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SUBJECT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ReinstateCaseTest {

    @InjectMocks
    private ReinstateCase reinstateCase;


    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        reinstateCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REINSTATE_CASE);
    }

    @Test
    void shouldSuccessfullyReinstateTheCaseEmail() {
        //Given
        final CaseData caseData = caseData();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().build())
            .documentEmailContent("content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        CaseDocumentsCIC caseDocumentsCIC = CaseDocumentsCIC.builder().applicantDocumentsUploaded(List.of(documentListValue)).build();
        CicCase cicCase = CicCase.builder()
            .reinstateReason(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR)
            .reinstateAdditionalDetail("some detail")
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .reinstateDocuments(caseDocumentsCIC)
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            reinstateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse responseReinstate =
            reinstateCase.reinstated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(responseReinstate).isNotNull();
        assertThat(responseReinstate.getConfirmationHeader()).contains("Subject");
        assertThat(responseReinstate.getConfirmationHeader()).contains("Respondent");
        assertThat(responseReinstate.getConfirmationHeader()).contains("Representative");
        assertThat(response.getData().getCicCase().getReinstateReason()).isNotNull();
        assertThat(response.getState()).isEqualTo(State.CaseManagement);
        assertThat(response.getData().getCicCase().getReinstateDocuments().getApplicantDocumentsUploaded()
            .get(0).getValue().getDocumentEmailContent()).isNotNull();

    }

    @Test
    void shouldSuccessfullyReinstateTheCasePost() {
        //Given
        final CaseData caseData = caseData();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().build())
            .documentEmailContent("content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        CaseDocumentsCIC caseDocumentsCIC = CaseDocumentsCIC.builder().applicantDocumentsUploaded(List.of(documentListValue)).build();
        CicCase cicCase = CicCase.builder()
            .reinstateReason(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR)
            .reinstateAdditionalDetail("some detail")
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .reinstateDocuments(caseDocumentsCIC)
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            reinstateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse responseReinstate =
            reinstateCase.reinstated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(responseReinstate).isNotNull();
        assertThat(responseReinstate.getConfirmationHeader()).contains("Subject");
        assertThat(responseReinstate.getConfirmationHeader()).contains("Respondent");
        assertThat(responseReinstate.getConfirmationHeader()).contains("Representative");
        assertThat(response.getData().getCicCase().getReinstateReason()).isNotNull();
        assertThat(response.getState()).isEqualTo(State.CaseManagement);
        assertThat(response.getData().getCicCase().getReinstateDocuments().getApplicantDocumentsUploaded()
            .get(0).getValue().getDocumentEmailContent()).isNotNull();

    }
}
