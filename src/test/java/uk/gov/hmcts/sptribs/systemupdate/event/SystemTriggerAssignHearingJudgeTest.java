package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskDateService;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Cancelled;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerAssignHearingJudge.SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemTriggerAssignHearingJudgeTest {

    @Mock
    private HearingJudgeTaskService hearingJudgeTaskService;

    @Mock
    private HearingJudgeTaskDateService dates;

    @Test
    void selectsUpcomingHearingAfterAnHistoricCancelledHearing() {
        LocalDate date = LocalDate.of(2026, 11, 1);
        Listing historic = Listing.builder().date(date.minusDays(30)).hearingStatus(Cancelled)
            .hearingJudgeTaskRequested(YES).build();
        Listing due = Listing.builder().date(date).hearingStatus(Listed).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().id("1").value(historic).build(),
            ListValue.<Listing>builder().id("2").value(due).build())).build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);
        when(dates.releaseDate(date)).thenReturn(date.minusDays(14));
        when(dates.isDue(date, date.minusDays(14))).thenReturn(true);

        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).aboutToSubmit(details, null);

        verify(hearingJudgeTaskService).updateTask(data, due);
        assertThat(data.getAssignHearingJudgeTaskTrigger()).isNull();
    }

    @Test
    void doesNotRequestForUnlistedHearing() {
        CaseData data = new CaseData();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);

        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).aboutToSubmit(details, null);

        verifyNoInteractions(hearingJudgeTaskService, dates);
    }

    @Test
    void doesNotRequestWhenHearingListIsNull() {
        CaseData data = CaseData.builder().hearingList(null).build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);

        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).aboutToSubmit(details, null);

        verifyNoInteractions(hearingJudgeTaskService, dates);
    }

    @Test
    void doesNotRequestWhenListingValueIsNull() {
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().id("1").value(null).build())).build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);

        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).aboutToSubmit(details, null);

        verifyNoInteractions(hearingJudgeTaskService, dates);
    }

    @Test
    void doesNotRequestWhenHearingTaskAlreadyRequested() {
        LocalDate date = LocalDate.of(2026, 11, 1);
        Listing listing = Listing.builder().date(date).hearingStatus(Listed)
            .hearingJudgeTaskRequested(YES).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().id("1").value(listing).build())).build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);

        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).aboutToSubmit(details, null);

        verifyNoInteractions(hearingJudgeTaskService, dates);
    }

    @Test
    void doesNotRequestWhenHearingIsNotDue() {
        LocalDate date = LocalDate.of(2026, 11, 1);
        Listing listing = Listing.builder().date(date).hearingStatus(Listed).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().id("1").value(listing).build())).build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);
        when(dates.releaseDate(date)).thenReturn(date.minusDays(14));
        when(dates.isDue(date, date.minusDays(14))).thenReturn(false);

        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).aboutToSubmit(details, null);

        verifyNoInteractions(hearingJudgeTaskService);
    }

    @Test
    void shouldConfigureEvent() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        new SystemTriggerAssignHearingJudge(hearingJudgeTaskService, dates).configure(configBuilder);
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE);
    }
}
