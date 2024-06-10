package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.CaseLinkedNotification;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_LINK_CASE;

@ExtendWith(MockitoExtension.class)
class CaseworkerLinkCaseTest {
    @InjectMocks
    private CaseworkerLinkCase caseWorkerLinkCase;

    @Mock
    private CaseLinkedNotification caseLinkedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        //When
        caseWorkerLinkCase.configure(configBuilder);
        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_LINK_CASE);
    }

    @Test
    void shouldSuccessfullyAddLinksToCase() {
        //Given
        final CaseData caseData = caseData();
        CicCase cicCase = new CicCase();
        cicCase.setSubjectCIC(Set.of(SubjectCIC.SUBJECT));
        cicCase.setApplicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC));
        cicCase.setRepresentativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        //When
        doNothing().when(caseLinkedNotification).sendToSubject(any(CaseData.class), eq(null));
        doNothing().when(caseLinkedNotification).sendToApplicant(any(CaseData.class), eq(null));
        doNothing().when(caseLinkedNotification).sendToRepresentative(any(CaseData.class), eq(null));
        SubmittedCallbackResponse response =
            caseWorkerLinkCase.submitted(updatedCaseDetails, beforeDetails);
        //Then
        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader()).contains("Case Link created");
    }

    @Test
    void shouldHandleNotificationFailureGracefully() {
        // Given
        final CaseData caseData = caseData();
        CicCase cicCase = new CicCase();
        cicCase.setSubjectCIC(Set.of(SubjectCIC.SUBJECT));
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        // When
        doThrow(new RuntimeException("Notification error")).when(caseLinkedNotification).sendToSubject(any(CaseData.class), anyString());
        SubmittedCallbackResponse response = caseWorkerLinkCase.submitted(updatedCaseDetails, beforeDetails);
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader()).contains("Case Link notification failed");
        assertThat(response.getConfirmationHeader()).contains("Please resend the notification");
    }
}
