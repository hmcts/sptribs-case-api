package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerAssignHearingJudge;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

class HearingJudgeTaskPublicationTest {

    @ParameterizedTest
    @MethodSource("triggerFields")
    void publishesHiddenReadOnlyTriggerField(CCDConfig<CaseData, State, UserRole> eventConfig,
                                             String eventId, String fieldId) {
        var configBuilder = createCaseDataConfigBuilder();
        eventConfig.configure(configBuilder);
        Event<CaseData, UserRole, State> event = getEventsFrom(configBuilder).get(eventId);

        assertThat(event).isNotNull();
        assertThat(event.isPublishToCamunda()).isTrue();
        Field<?, ?, ?, ?> field = event.getFields().getFields().stream()
            .map(builder -> (Field<?, ?, ?, ?>) builder.build())
            .filter(candidate -> fieldId.equals(candidate.getId()))
            .findFirst().orElseThrow();

        assertThat(field.getContext()).isEqualTo(DisplayContext.ReadOnly);
        assertThat(field.getShowCondition()).isEqualTo("[STATE]=\"NEVER_SHOW\"");
        assertThat(field.isShowSummary()).isFalse();
    }

    static Stream<Arguments> triggerFields() {
        var recordListing = new CaseworkerRecordListing(null, null, null, null);
        var editRecordListing = new CaseworkerEditRecordListing(mock(RecordListHelper.class), null, null, null);
        var postpone = new CaseworkerPostponeHearing(null, null, null, null);
        var cancel = new CaseworkerCancelHearing(null, null, null);
        var scheduled = new SystemTriggerAssignHearingJudge(null, null);

        return Stream.of(
            Arguments.of(recordListing, "caseworker-record-listing", "assignHearingJudgeTaskTrigger"),
            Arguments.of(editRecordListing, "caseworker-edit-record-listing", "assignHearingJudgeTaskTrigger"),
            Arguments.of(editRecordListing, "caseworker-edit-record-listing", "cancelHearingJudgeTaskTrigger"),
            Arguments.of(postpone, "caseworker-postpone-hearing", "cancelHearingJudgeTaskTrigger"),
            Arguments.of(cancel, "caseworker-cancel-hearing", "cancelHearingJudgeTaskTrigger"),
            Arguments.of(scheduled, "system-trigger-assign-hearing-judge", "assignHearingJudgeTaskTrigger")
        );
    }
}
