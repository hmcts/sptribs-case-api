package uk.gov.hmcts.sptribs.dmn;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.sptribs.DmnDecisionTableBaseUnitTest;
import uk.gov.hmcts.sptribs.caseworker.event.CaseworkerRecordListing;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskDateService;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskService;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.DmnDecisionTable.WA_TASK_INITIATION_ST_CIC_CRIMINALINJURIESCOMPENSATION;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

class HearingJudgeTaskEventToDmnTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void selectDecision() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_INITIATION_ST_CIC_CRIMINALINJURIESCOMPENSATION;
    }

    @Test
    void newlyRecordedDueHearingPublishesTriggerUsedToInitiateTask() {
        LocalDate hearingDate = LocalDate.of(2026, 11, 1);
        HearingJudgeTaskDateService dates = mock(HearingJudgeTaskDateService.class);
        when(dates.releaseDate(hearingDate)).thenReturn(LocalDate.of(2026, 10, 18));
        when(dates.isDue(hearingDate, LocalDate.of(2026, 10, 18))).thenReturn(true);

        RecordListHelper helper = mock(RecordListHelper.class);
        Listing listing = Listing.builder().date(hearingDate).build();
        when(helper.checkAndUpdateVenueInformation(listing)).thenReturn(listing);
        CaseworkerRecordListing eventConfig = new CaseworkerRecordListing(mock(HearingService.class),
            new HearingJudgeTaskService(dates), helper, null);
        var configBuilder = createCaseDataConfigBuilder();
        eventConfig.configure(configBuilder);
        Event<CaseData, UserRole, State> event = getEventsFrom(configBuilder).get("caseworker-record-listing");

        CaseData caseData = CaseData.builder().listing(listing).build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        var response = eventConfig.aboutToSubmit(details, new CaseDetails<>());
        String attemptKey = response.getData().getAssignHearingJudgeTaskTrigger();

        assertThat(attemptKey).isNotBlank();
        assertThat(event.isPublishToCamunda()).isTrue();
        assertThat(event.getFields().getFields().stream()
            .anyMatch(field -> "assignHearingJudgeTaskTrigger".equals(field.build().getId()))).isTrue();

        Map<String, Object> payload = Map.of("Data", Map.of("assignHearingJudgeTaskTrigger", attemptKey));
        var result = evaluateDmnTable(Map.of("eventId", event.getId(), "postEventState",
            response.getState().name(), "additionalData", payload)).getResultList();

        assertThat(result).anySatisfy(task -> {
            assertThat(task).containsEntry("taskId", "assignHearingJudge");
            assertThat(task).containsEntry("name", "Assign JOH to hearing");
            assertThat(task).containsEntry("processCategories", "HearingJudgeAssignment_" + attemptKey);
        });
    }
}
