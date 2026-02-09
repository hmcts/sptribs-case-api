package uk.gov.hmcts.sptribs.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;
import uk.gov.hmcts.sptribs.notification.dispatcher.ApplicationReceivedNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.time.LocalDate;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.caseworker.model.SecurityClass.PUBLIC;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CreateCaseTest {

    @Mock
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private ApplicationReceivedNotification applicationReceivedNotification;

    @InjectMocks
    private CreateCase createCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        createCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("caseworker-create-case");
    }

    @Test
    void shouldSuccessfullyTriggerAboutToSubmitEventOnCreateCase() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setFullName("Test Full Name");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getState()).isEqualTo(Submitted);
        assertThat(result.getData().getSecurityClass()).isEqualTo(PUBLIC);
        assertThat(result.getData().getCaseNameHmctsInternal()).isEqualTo("Test Full Name");
        assertThat(result.getData().getNewBundleOrderEnabled())
            .isEqualTo(YesNo.YES);
    }

    @Test
    void shouldSetIsRepresentativePresentToYesWhenAboutToSubmitEventTriggered() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setRepresentativeFullName("Test Representative Full Name");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCicCase().getIsRepresentativePresent()).isEqualTo(YES);
    }

    @Test
    void shouldSetIsRepresentativePresentToNoWhenAboutToSubmitEventTriggered() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setRepresentativeFullName(null);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCicCase().getIsRepresentativePresent()).isEqualTo(NO);
    }

    @Test
    void shouldSuccessfullyPopulateCaseFlagsWhenAboutToSubmitEventTriggered() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setFullName("Test Full Name");
        caseData.getCicCase().setApplicantFullName("Test Applicant Full Name");
        caseData.getCicCase().setRepresentativeFullName("Test Representative Full Name");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCaseFlags()).isNotNull();

        assertThat(result.getData().getSubjectFlags()).isNotNull();
        assertThat(result.getData().getSubjectFlags().getPartyName()).isEqualTo("Test Full Name");
        assertThat(result.getData().getSubjectFlags().getRoleOnCase()).isEqualTo("subject");

        assertThat(result.getData().getApplicantFlags()).isNotNull();
        assertThat(result.getData().getApplicantFlags().getPartyName()).isEqualTo("Test Applicant Full Name");
        assertThat(result.getData().getApplicantFlags().getRoleOnCase()).isEqualTo("applicant");

        assertThat(result.getData().getRepresentativeFlags()).isNotNull();
        assertThat(result.getData().getRepresentativeFlags().getPartyName()).isEqualTo("Test Representative Full Name");
        assertThat(result.getData().getRepresentativeFlags().getRoleOnCase()).isEqualTo("Representative");
    }

    @Test
    void shouldSuccessfullyTriggerSubmittedEventOnCreateCase() {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(caseData.formatCaseRef(TEST_CASE_ID));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        SubmittedCallbackResponse result = createCase.submitted(caseDetails, caseDetails);

        assertThat(result.getConfirmationHeader())
            .isEqualTo("# Case Created \n## Case reference number: \n## 1616-5914-0147-3378");
    }

    @Test
    void shouldNotSuccessfullyTriggerSubmittedEventOnCreateCaseIfNotificationsFail() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.getCicCase().setSubjectCIC(Set.of(SUBJECT));
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        doThrow(NotificationException.class)
            .when(applicationReceivedNotification).sendToSubject(caseData, hyphenatedCaseRef);

        SubmittedCallbackResponse result = createCase.submitted(caseDetails, caseDetails);

        assertThat(result.getConfirmationHeader())
            .isEqualTo("# Create case notification failed \n## Please resend the notification");
    }

    @Test
    void shouldSubmitSupplementaryDataToCcdWhenSubmittedEventTriggered() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        createCase.submitted(caseDetails, caseDetails);

        verify(ccdSupplementaryDataService).submitSupplementaryDataToCcd(TEST_CASE_ID.toString());
    }

    @Test
    void shouldSendApplicationReceivedNotificationsWhenSubmittedEventTriggered() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setSubjectCIC(Set.of(SUBJECT));
        caseData.getCicCase().setApplicantCIC(Set.of(APPLICANT_CIC));
        caseData.getCicCase().setRepresentativeCIC(Set.of(REPRESENTATIVE));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        createCase.submitted(caseDetails, caseDetails);

        verify(applicationReceivedNotification)
            .sendToSubject(caseData, hyphenatedCaseRef);
        verify(applicationReceivedNotification)
            .sendToApplicant(caseData, hyphenatedCaseRef);
        verify(applicationReceivedNotification)
            .sendToRepresentative(caseData, hyphenatedCaseRef);
    }

    @Test
    void shouldNotSendApplicationReceivedNotificationsWhenSubmittedEventTriggered() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setSubjectCIC(emptySet());
        caseData.getCicCase().setApplicantCIC(emptySet());
        caseData.getCicCase().setRepresentativeCIC(emptySet());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        createCase.submitted(caseDetails, caseDetails);

        verifyNoInteractions(applicationReceivedNotification);
    }

    @Test
    void shouldCalculateAndSetIsCaseInTimeAsYesForInitialCicaDecisionDateEqualToCaseReceivedDate() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setFullName("Test Full Name");
        caseData.getCicCase().setCaseReceivedDate(LocalDate.now());
        caseData.getCicCase().setInitialCicaDecisionDate(LocalDate.now());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCicCase().getInitialCicaDecisionDate()).isEqualTo(LocalDate.now());
        assertThat(result.getData().getCicCase().getIsCaseInTime()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldCalculateAndSetIsCaseInTimeAsYesForCaseReceivedDateEqualTo90DaysFromInitialCicaDecisionDate() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setFullName("Test Full Name");
        caseData.getCicCase().setCaseReceivedDate(LocalDate.now().plusDays(90));
        caseData.getCicCase().setInitialCicaDecisionDate(LocalDate.now());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCicCase().getInitialCicaDecisionDate()).isEqualTo(LocalDate.now());
        assertThat(result.getData().getCicCase().getIsCaseInTime()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldCalculateAndSetIsCaseInTimeAsNoForCaseReceivedDateGreaterThan90DaysFromInitialCicaDecisionDate() {
        final CaseData caseData = caseData();
        caseData.getCicCase().setFullName("Test Full Name");
        caseData.getCicCase().setCaseReceivedDate(LocalDate.now().plusDays(91));
        caseData.getCicCase().setInitialCicaDecisionDate(LocalDate.now());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> result =
            createCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCicCase().getInitialCicaDecisionDate()).isEqualTo(LocalDate.now());
        assertThat(result.getData().getCicCase().getIsCaseInTime()).isEqualTo(YesOrNo.NO);
    }
}
