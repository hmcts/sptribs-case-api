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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.AdditionalDocument;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.CaseIssuedNotification;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_CASE;


@ExtendWith(MockitoExtension.class)
class CaseworkerIssueCaseTest {

    @InjectMocks
    private CaseworkerIssueCase caseworkerIssueCase;

    @Mock
    private CaseIssuedNotification caseIssuedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerIssueCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_CASE);
    }

    @Test
    void shouldSuccessfullyIssueTheCase() {
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

        final CaseIssue caseIssue = new CaseIssue();
        caseIssue.setAdditionalDocument(Set.of(AdditionalDocument.APPLICANT_EVIDENCE));
        caseData.setCaseIssue(caseIssue);
        caseData.setHyphenatedCaseRef("1234-5678-3456");

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerIssueCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        Mockito.doNothing().when(caseIssuedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(caseIssuedNotification).sendToApplicant(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(caseIssuedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(caseIssuedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        SubmittedCallbackResponse issuedResponse = caseworkerIssueCase.issued(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getCaseIssue().getAdditionalDocument()).doesNotContain(AdditionalDocument.TRIBUNAL_FORM);
        assertThat(response.getData().getCicCase().getNotifyPartyApplicant()).isNotNull();
        assertThat(issuedResponse).isNotNull();
    }
}
