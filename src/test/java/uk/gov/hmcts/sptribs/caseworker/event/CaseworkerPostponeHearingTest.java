package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHaringNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_POSTPONE_HEARING;

@ExtendWith(MockitoExtension.class)
class CaseworkerPostponeHearingTest {
    @InjectMocks
    private CaseWorkerPostponeHearing caseWorkerPostponeHearing;

    @Mock
    private HearingService hearingService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerPostponeHearing.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_POSTPONE_HEARING);
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        when(hearingService.getHearingDateDynamicList(any())).thenReturn(null);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerPostponeHearing.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_POSTPONE_HEARING);
    }

    @InjectMocks
    private PostponeHaringNotifyParties postponeHaringNotifyParties;

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = postponeHaringNotifyParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldSuccessfullyPostpone() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .recordNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .recordNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = caseWorkerPostponeHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submitted = caseWorkerPostponeHearing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getCicCase().getHearingNotificationParties()).hasSize(3);
        assertThat(submitted.getConfirmationHeader()).contains("Hearing Postponed");
        assertThat(response.getData().getCicCase().getHearingNotificationParties()).contains(NotificationParties.SUBJECT);
    }
}
