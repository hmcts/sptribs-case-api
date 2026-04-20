package uk.gov.hmcts.sptribs.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.sptribs.DmnDecisionTableBaseUnitTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.sptribs.DmnDecisionTable.WA_TASK_TYPES_ST_CIC_CRIMINALINJURIESCOMPENSATION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_ADMIN_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_CTSC_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_JUDICIARY_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_LO_TASK;

class CamundaTaskTypesTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_TYPES_ST_CIC_CRIMINALINJURIESCOMPENSATION;
    }

    static Stream<Arguments> scenarioProvider() {
        List<Map<String, String>> taskTypes = List.of(
            Map.of(
                "taskTypeId", REVIEW_SPECIFIC_ACCESS_REQ_JUDICIARY_TASK,
                "taskTypeName", "Review Specific Access Request Judiciary"
            ),
            Map.of(
                "taskTypeId", REVIEW_SPECIFIC_ACCESS_REQ_LO_TASK,
                "taskTypeName", "Review Specific Access Request Legal Ops"
            ),
            Map.of(
                "taskTypeId", REVIEW_SPECIFIC_ACCESS_REQ_ADMIN_TASK,
                "taskTypeName", "Review Specific Access Request Admin"
            ),
            Map.of(
                "taskTypeId", REVIEW_SPECIFIC_ACCESS_REQ_CTSC_TASK,
                    "taskTypeName", "Review Specific Access Request CTSC"
            )
        );
        return Stream.of(
            Arguments.of(
                taskTypes
            )
        );
    }

    @Test
    void check_dmn_changed() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getInputs().size(), is(1));
        assertThat(logic.getOutputs().size(), is(2));
        assertThat(logic.getRules().size(), is(4));
    }

    @ParameterizedTest(name = "retrieve all task type data")
    @MethodSource("scenarioProvider")
    void should_evaluate_dmn_return_all_task_type_fields(List<Map<String, Object>> expectedTaskTypes) {

        VariableMap inputVariables = new VariableMapImpl();
        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(expectedTaskTypes));
    }

}
