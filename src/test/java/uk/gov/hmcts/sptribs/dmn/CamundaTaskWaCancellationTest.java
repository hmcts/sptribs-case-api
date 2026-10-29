package uk.gov.hmcts.sptribs.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.sptribs.DmnDecisionTableBaseUnitTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static uk.gov.hmcts.sptribs.DmnDecisionTable.WA_TASK_CANCELLATION_ST_CIC_CRIMINALINJURIESCOMPENSATION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_CATEGORY_HEARING_BUNDLE;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_CATEGORY_HEARING_COMPLETION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_CATEGORY_ISSUE_CASE;
import static uk.gov.hmcts.sptribs.dmnutils.CancellationScenarioBuilder.event;

class CamundaTaskWaCancellationTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_CANCELLATION_ST_CIC_CRIMINALINJURIESCOMPENSATION;
    }

    public static Stream<Arguments> scenarioProvider() {
        return Stream.of(
            event("caseworker-close-the-case")
                .cancelAll()
                .build(),
            event("caseworker-postpone-hearing")
                .cancel(PROCESS_CATEGORY_HEARING_COMPLETION)
                .cancel(PROCESS_CATEGORY_HEARING_BUNDLE)
                .reconfigureAll()
                .build(),
            event("caseworker-cancel-hearing")
                .cancel(PROCESS_CATEGORY_HEARING_COMPLETION)
                .cancel(PROCESS_CATEGORY_HEARING_BUNDLE)
                .reconfigureAll()
                .build(),
            event("caseworker-record-listing")
                .reconfigureAll()
                .build(),
            event("caseworker-edit-record-listing")
                .reconfigureAll()
                .build(),
            event("create-hearing-summary")
                .reconfigureAll()
                .build(),
            event("refer-to-judge")
                .cancel(PROCESS_CATEGORY_ISSUE_CASE)
                .build(),
            event("refer-to-legal-officer")
                .cancel(PROCESS_CATEGORY_ISSUE_CASE)
                .build()
        );
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getInputs().size(), is(4));
        assertThat(logic.getOutputs().size(), is(4));
        assertThat(logic.getRules().size(), is(6));
    }

    @Test
    void cancelsOnlyTheHearingWhoseAttemptKeyWasPublished() {
        VariableMap input = new VariableMapImpl();
        input.putValue("event", "caseworker-edit-record-listing");
        input.putValue("additionalData", Map.of("Data", Map.of("cancelHearingJudgeTaskTrigger", "hearing-one")));

        assertThat(evaluateDmnTable(input).getResultList(), is(List.of(
            Map.of("action", "Reconfigure"),
            Map.of("action", "Cancel", "processCategories", "HearingJudgeAssignment_hearing-one")
        )));
    }

    @Test
    void doesNotCancelHearingJudgeTaskWithoutCancellationKey() {
        VariableMap input = new VariableMapImpl();
        input.putValue("event", "caseworker-edit-record-listing");
        input.putValue("additionalData", Map.of("Data", Map.of()));

        assertThat(evaluateDmnTable(input).getResultList(), is(List.of(Map.of("action", "Reconfigure"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-postpone-hearing", "caseworker-cancel-hearing"
    })
    void cancelsOnlyThePublishedHearingAttemptOnPostponementOrCancellation(String eventId) {
        VariableMap input = new VariableMapImpl();
        input.putValue("event", eventId);
        input.putValue("additionalData", Map.of("Data", Map.of("cancelHearingJudgeTaskTrigger", "hearing-one")));

        assertThat(evaluateDmnTable(input).getResultList(), hasItem(
            Map.of("action", "Cancel", "processCategories", "HearingJudgeAssignment_hearing-one")));
    }

    @Test
    void usesDifferentProcessCategoriesForDifferentHearingsOnTheSameCase() {
        VariableMap input = new VariableMapImpl();
        input.putValue("event", "caseworker-edit-record-listing");
        input.putValue("additionalData", Map.of("Data", Map.of("cancelHearingJudgeTaskTrigger", "hearing-two")));

        assertThat(evaluateDmnTable(input).getResultList(), is(List.of(
            Map.of("action", "Reconfigure"),
            Map.of("action", "Cancel", "processCategories", "HearingJudgeAssignment_hearing-two")
        )));
    }

    @ParameterizedTest(name = "from state: {0}, event id: {1}, state: {2}")
    @MethodSource("scenarioProvider")
    void given_multiple_event_ids_should_evaluate_dmn(String fromState,
                                                      String eventId,
                                                      String state,
                                                      List<Map<String, Object>> expectedDmnOutcome) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("fromState", fromState);
        inputVariables.putValue("event", eventId);
        inputVariables.putValue("state", state);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(expectedDmnOutcome));
    }

}
