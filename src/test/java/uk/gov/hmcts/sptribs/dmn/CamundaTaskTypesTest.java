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
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.COMPLETE_HEARING_OUTCOME_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.CREATE_DUE_DATE;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.FOLLOW_UP_NONCOMPLIANCE_OF_DIR_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ISSUE_CASE_TO_RESPONDENT_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ISSUE_DECISION_NOTICE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ISSUE_DUE_DATE;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_CASE_WITHDRAWAL_DIR_LISTED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_CASE_WITHDRAWAL_DIR_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_CORRECTIONS_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_DIR_RELISTED_CASE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_DIR_RELISTED_CASE_WITHIN_5DAYS_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_DIR_RETURNED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_FURTHER_EVIDENCE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_LISTING_DIR_LISTED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_LISTING_DIR_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_OTHER_DIR_RETURNED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_POSTPONEMENT_DIR_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_REINSTATEMENT_DECISION_NOTICE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_RULE27_DECISION_LISTED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_RULE27_DECISION_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_SET_ASIDE_DIR_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_STAY_DIR_LISTED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_STAY_DIR_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_STRIKE_OUT_DIR_RETURNED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_TIME_EXT_DIR_RETURNED_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PROCESS_WRITTEN_REASONS_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REGISTER_NEW_CASE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_CORRECTIONS_REQ_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LISTING_DIR_CASE_LISTED_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LISTING_DIR_CASE_LISTED_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LISTING_DIR_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LISTING_DIR_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LIST_CASE_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LIST_CASE_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LIST_CASE_WITHIN_5DAYS_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_LIST_CASE_WITHIN_5DAYS_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_NEW_CASE_PROVIDE_DIR_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_NEW_CASE_PROVIDE_DIR_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_ORDER;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_OTHER_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_OTHER_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_POSTPONEMENT_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_POSTPONEMENT_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_REINSTATEMENT_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_REINSTATEMENT_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_RULE27_REQ_CASE_LISTED_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_RULE27_REQ_CASE_LISTED_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_RULE27_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_RULE27_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SET_ASIDE_REQ_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_ADMIN_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_CTSC_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_JUDICIARY_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_STAY_REQ_CASE_LISTED_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_STAY_REQ_CASE_LISTED_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_STAY_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_STAY_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_STRIKE_OUT_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_STRIKE_OUT_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_TIME_EXT_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_TIME_EXT_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_WITHDRAWAL_REQ_CASE_LISTED_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_WITHDRAWAL_REQ_CASE_LISTED_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_WITHDRAWAL_REQ_JUDGE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_WITHDRAWAL_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_WRITTEN_REASONS_REQ_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.STITCH_COLLATE_HEARING_BUNDLE_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.VET_NEW_CASE_DOCUMENTS_TASK;

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
