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
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHearingNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.HearingPostponedNotification;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_POSTPONE_HEARING;

@ExtendWith(MockitoExtension.class)
class CaseworkerPostponeHearingTest {
    @InjectMocks
    private CaseworkerPostponeHearing caseWorkerPostponeHearing;

    @Mock
    private RecordListHelper recordListHelper;

    @Mock
    private HearingService hearingService;

    @Mock
    private HearingPostponedNotification hearingPostponedNotification;

    @InjectMocks
    private PostponeHearingNotifyParties postponeHearingNotifyParties;

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
        when(hearingService.getListedHearingDynamicList(any())).thenReturn(null);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerPostponeHearing.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_POSTPONE_HEARING);
    }

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = postponeHearingNotifyParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldSuccessfullyPostpone() {
        //Given
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingNotificationParties(parties)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        doNothing().when(hearingPostponedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(hearingPostponedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(hearingPostponedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(recordListHelper).getNotificationParties(any());

        AboutToStartOrSubmitResponse<CaseData, State> response
            = caseWorkerPostponeHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submitted = caseWorkerPostponeHearing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(submitted.getConfirmationHeader()).contains("Hearing Postponed");
        assert (response.getData().getListing().getHearingStatus().equals(HearingState.Postponed));
    }
}
