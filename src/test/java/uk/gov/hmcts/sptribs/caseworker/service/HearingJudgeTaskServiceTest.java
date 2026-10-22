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
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
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
        assertThat(hearing.getHearingJudgeTaskRequested()).isEqualTo(YES);
        assertThat(caseData.getAssignHearingJudgeTaskTrigger()).isEqualTo(hearing.getHearingJudgeTaskId());
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
}
