package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.NO;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Cancelled;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;

@ExtendWith(MockitoExtension.class)
class HearingJudgeTaskServiceTest {

    @Mock
    private HearingJudgeTaskDateService dates;

    @Test
    void requestsTaskImmediatelyForHearingInsideWindow() {
        CaseData caseData = new CaseData();
        LocalDate hearingDate = LocalDate.of(2026, 11, 1);
        Listing hearing = Listing.builder().date(hearingDate).hearingStatus(Listed).build();
        when(dates.releaseDate(hearingDate)).thenReturn(LocalDate.of(2026, 10, 18));
        when(dates.isDue(hearingDate, LocalDate.of(2026, 10, 18))).thenReturn(true);

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(hearing.getHearingJudgeTaskId()).isNotBlank();
        assertThat(hearing.getHearingJudgeHearingId()).isNotBlank();
        assertThat(hearing.getHearingJudgeTaskRequested()).isEqualTo(YES);
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isEqualTo(hearing.getHearingJudgeTaskId());
    }

    @Test
    void requestsTaskEvenWhenCaseHasAJudgeFromAnEarlierHearing() {
        CaseData caseData = CaseData.builder().judicialId("previous-hearing-judge").build();
        LocalDate hearingDate = LocalDate.of(2026, 11, 1);
        Listing hearing = Listing.builder().date(hearingDate).hearingStatus(Listed).build();
        when(dates.releaseDate(hearingDate)).thenReturn(LocalDate.of(2026, 10, 18));
        when(dates.isDue(hearingDate, LocalDate.of(2026, 10, 18))).thenReturn(true);

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(hearing.getHearingJudgeTaskRequested()).isEqualTo(YES);
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isEqualTo(hearing.getHearingJudgeTaskId());
        assertThat(caseData.getJudicialId()).isEqualTo("previous-hearing-judge");
    }

    @Test
    void doesNotRequestTaskTwiceForSameHearing() {
        CaseData caseData = new CaseData();
        Listing hearing = Listing.builder().date(LocalDate.of(2026, 11, 1))
            .hearingStatus(Listed).hearingJudgeTaskId("stable-id").hearingJudgeTaskRequested(YES).build();
        when(dates.releaseDate(hearing.getDate())).thenReturn(LocalDate.of(2026, 10, 18));
        when(dates.isDue(hearing.getDate(), LocalDate.of(2026, 10, 18))).thenReturn(true);

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(hearing.getHearingJudgeTaskId()).isEqualTo("stable-id");
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isNull();
    }

    @Test
    void doesNotRequestTaskBeforeWindow() {
        CaseData caseData = new CaseData();
        Listing hearing = Listing.builder().date(LocalDate.of(2026, 12, 1)).hearingStatus(Listed).build();
        when(dates.releaseDate(hearing.getDate())).thenReturn(LocalDate.of(2026, 11, 16));

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(hearing.getHearingJudgeTaskReleaseDate()).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isNull();
    }

    @Test
    void requestsCancellationOnlyForTheAmendedHearing() {
        CaseData caseData = new CaseData();
        LocalDate laterDate = LocalDate.of(2026, 12, 1);
        Listing amended = Listing.builder().date(laterDate).hearingStatus(Listed)
            .hearingJudgeTaskId("attempt-one").hearingJudgeTaskRequested(YES).build();
        when(dates.releaseDate(laterDate)).thenReturn(LocalDate.of(2026, 11, 17));

        new HearingJudgeTaskService(dates).updateTask(caseData, amended);

        assertThat(caseData.getCancelHearingJudgeTaskTrigger()).isEqualTo("attempt-one");
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isNull();
        assertThat(amended.getHearingJudgeTaskRequested()).isEqualTo(NO);
        assertThat(amended.getHearingJudgeTaskId()).isNull();
        assertThat(amended.getHearingJudgeHearingId()).isNotBlank();
    }

    @Test
    void doesNotCancelAnUnrequestedTask() {
        CaseData caseData = new CaseData();
        Listing hearing = Listing.builder().date(LocalDate.of(2026, 12, 1)).hearingStatus(Listed).build();
        when(dates.releaseDate(hearing.getDate())).thenReturn(LocalDate.of(2026, 11, 17));

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(caseData.getCancelHearingJudgeTaskTrigger()).isNull();
    }

    @Test
    void createsANewAttemptIfHearingMovesBackIntoWindow() {
        CaseData caseData = new CaseData();
        LocalDate hearingDate = LocalDate.of(2026, 11, 1);
        Listing hearing = Listing.builder().date(hearingDate).hearingStatus(Listed)
            .hearingJudgeTaskRequested(NO).build();
        when(dates.releaseDate(hearingDate)).thenReturn(LocalDate.of(2026, 10, 18));
        when(dates.isDue(hearingDate, LocalDate.of(2026, 10, 18))).thenReturn(true);

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(hearing.getHearingJudgeTaskId()).isNotBlank().isNotEqualTo("attempt-one");
        assertThat(caseData.getCancelHearingJudgeTaskTrigger()).isNull();
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isEqualTo(hearing.getHearingJudgeTaskId());
    }

    @Test
    void cancelsRequestedTaskWhenHearingIsCancelled() {
        CaseData caseData = new CaseData();
        Listing hearing = Listing.builder().date(LocalDate.of(2026, 11, 1))
            .hearingStatus(Cancelled).hearingJudgeTaskId("attempt-one").hearingJudgeTaskRequested(YES).build();

        new HearingJudgeTaskService(dates).updateTask(caseData, hearing);

        assertThat(hearing.getHearingJudgeTaskReleaseDate()).isNull();
        assertThat(caseData.getCancelHearingJudgeTaskTrigger()).isEqualTo("attempt-one");
    }

    @Test
    void keepsHearingIdentityButChangesAttemptAfterLeavingAndReenteringWindow() {
        final CaseData caseData = new CaseData();
        LocalDate insideWindow = LocalDate.of(2026, 11, 1);
        LocalDate outsideWindow = LocalDate.of(2026, 12, 1);
        final Listing hearing = Listing.builder().date(insideWindow).hearingStatus(Listed).build();
        when(dates.releaseDate(insideWindow)).thenReturn(LocalDate.of(2026, 10, 18));
        when(dates.isDue(insideWindow, LocalDate.of(2026, 10, 18))).thenReturn(true);
        when(dates.releaseDate(outsideWindow)).thenReturn(LocalDate.of(2026, 11, 17));
        HearingJudgeTaskService service = new HearingJudgeTaskService(dates);

        service.updateTask(caseData, hearing);
        String firstAttempt = hearing.getHearingJudgeTaskId();
        final String hearingId = hearing.getHearingJudgeHearingId();

        hearing.setDate(outsideWindow);
        service.updateTask(caseData, hearing);
        assertThat(caseData.getCancelHearingJudgeTaskTrigger()).isEqualTo(firstAttempt);

        hearing.setDate(insideWindow);
        service.updateTask(caseData, hearing);
        assertThat(hearing.getHearingJudgeHearingId()).isEqualTo(hearingId);
        assertThat(hearing.getHearingJudgeTaskId()).isNotEqualTo(firstAttempt);
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isEqualTo(hearing.getHearingJudgeTaskId());
        assertThat(caseData.getCancelHearingJudgeTaskTrigger()).isNull();
    }
}
