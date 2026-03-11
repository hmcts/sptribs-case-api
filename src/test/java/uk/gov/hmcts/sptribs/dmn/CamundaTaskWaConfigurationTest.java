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
import uk.gov.hmcts.sptribs.DmnDecisionTableBaseUnitTest;
import uk.gov.hmcts.sptribs.dmnutils.CaseDataBuilder;
import uk.gov.hmcts.sptribs.dmnutils.ConfigurationExpectationBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.DmnDecisionTable.WA_TASK_CONFIGURATION_ST_CIC_CRIMINALINJURIESCOMPENSATION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ACCESS_WORK_TYPE;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ADDITIONAL_PROPERTIES_ROLE_ASSIGNMENT_ID;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DEFAULT_MAJOR_PRIORITY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DEFAULT_MINOR_PRIORITY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DESCRIPTION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DUE_DATE_INTERVAL_DAYS;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DUE_DATE_ORIGIN;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.MAJOR_PRIORITY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.MINOR_PRIORITY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PRIORITY_DATE_ORIGIN_REF;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_ADMIN_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_CTSC_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_JUDICIARY_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REVIEW_SPECIFIC_ACCESS_REQ_LO_TASK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ROLE_CATEGORY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ROLE_CATEGORY_ADMIN;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ROLE_CATEGORY_CTSC;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ROLE_CATEGORY_JUDICIAL;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ROLE_CATEGORY_LO;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.WORK_TYPE;

class CamundaTaskWaConfigurationTest extends DmnDecisionTableBaseUnitTest {

    private static final String taskId = UUID.randomUUID().toString();
    private static final String roleAssignmentId = UUID.randomUUID().toString();

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_CONFIGURATION_ST_CIC_CRIMINALINJURIESCOMPENSATION;
    }

    static Stream<Arguments> scenarioProvider() throws IOException {
        return Stream.of(
            Arguments.of(
                REVIEW_SPECIFIC_ACCESS_REQ_JUDICIARY_TASK,
                CaseDataBuilder.defaultCase().build(),
                ConfigurationExpectationBuilder.defaultExpectations()
                    .expectedValue(MINOR_PRIORITY, DEFAULT_MINOR_PRIORITY, true)
                    .expectedValue(MAJOR_PRIORITY, DEFAULT_MAJOR_PRIORITY, true)
                    .expectedValue(WORK_TYPE, ACCESS_WORK_TYPE, true)
                    .expectedValue(ROLE_CATEGORY, ROLE_CATEGORY_JUDICIAL, true)
                    .expectedValue(DUE_DATE_INTERVAL_DAYS, "2", true)
                    .expectedValue(
                        DESCRIPTION,
                        "[Review Access Request](/role-access/" + taskId + "/assignment/" + roleAssignmentId + "/specific-access)",
                        true
                    )
                    .expectedValue(ADDITIONAL_PROPERTIES_ROLE_ASSIGNMENT_ID, roleAssignmentId, false)
                    .build()
            ),
            Arguments.of(
                REVIEW_SPECIFIC_ACCESS_REQ_LO_TASK,
                CaseDataBuilder.defaultCase().build(),
                ConfigurationExpectationBuilder.defaultExpectations()
                    .expectedValue(MINOR_PRIORITY, DEFAULT_MINOR_PRIORITY, true)
                    .expectedValue(MAJOR_PRIORITY, DEFAULT_MAJOR_PRIORITY, true)
                    .expectedValue(WORK_TYPE, ACCESS_WORK_TYPE, true)
                    .expectedValue(ROLE_CATEGORY, ROLE_CATEGORY_LO, true)
                    .expectedValue(DUE_DATE_INTERVAL_DAYS, "2", true)
                    .expectedValue(
                        DESCRIPTION,
                        "[Review Access Request](/role-access/" + taskId + "/assignment/" + roleAssignmentId + "/specific-access)",
                        true
                    )
                    .expectedValue(ADDITIONAL_PROPERTIES_ROLE_ASSIGNMENT_ID, roleAssignmentId, false)
                    .build()
            ),
            Arguments.of(
                REVIEW_SPECIFIC_ACCESS_REQ_ADMIN_TASK,
                CaseDataBuilder.defaultCase().build(),
                ConfigurationExpectationBuilder.defaultExpectations()
                    .expectedValue(MINOR_PRIORITY, DEFAULT_MINOR_PRIORITY, true)
                    .expectedValue(MAJOR_PRIORITY, DEFAULT_MAJOR_PRIORITY, true)
                    .expectedValue(WORK_TYPE, ACCESS_WORK_TYPE, true)
                    .expectedValue(ROLE_CATEGORY, ROLE_CATEGORY_ADMIN, true)
                    .expectedValue(DUE_DATE_INTERVAL_DAYS, "2", true)
                    .expectedValue(
                        DESCRIPTION,
                        "[Review Access Request](/role-access/" + taskId + "/assignment/" + roleAssignmentId + "/specific-access)",
                        true
                    )
                    .expectedValue(ADDITIONAL_PROPERTIES_ROLE_ASSIGNMENT_ID, roleAssignmentId, false)
                    .build()
            ),
            Arguments.of(
                REVIEW_SPECIFIC_ACCESS_REQ_CTSC_TASK,
                CaseDataBuilder.defaultCase().build(),
                ConfigurationExpectationBuilder.defaultExpectations()
                    .expectedValue(MINOR_PRIORITY, DEFAULT_MINOR_PRIORITY, true)
                    .expectedValue(MAJOR_PRIORITY, DEFAULT_MAJOR_PRIORITY, true)
                    .expectedValue(WORK_TYPE, ACCESS_WORK_TYPE, true)
                    .expectedValue(ROLE_CATEGORY, ROLE_CATEGORY_CTSC, true)
                    .expectedValue(DUE_DATE_INTERVAL_DAYS, "2", true)
                    .expectedValue(
                        DESCRIPTION,
                        "[Review Access Request](/role-access/" + taskId + "/assignment/" + roleAssignmentId + "/specific-access)",
                        true
                    )
                    .expectedValue(ADDITIONAL_PROPERTIES_ROLE_ASSIGNMENT_ID, roleAssignmentId, false)
                    .build()
            )
        );
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getInputs().size(), is(2));
        assertThat(logic.getOutputs().size(), is(3));
        assertEquals(45, logic.getRules().size());
    }

    @ParameterizedTest(name = "task type: {0} case data: {1}")
    @MethodSource("scenarioProvider")
    void should_return_correct_configuration_values_for_scenario(
        String taskType, Map<String, Object> caseData,
        List<Map<String, Object>> expectation) {
        VariableMap inputVariables = new VariableMapImpl();

        Map<String, String> taskAttributes = Map.of(
            "taskType", taskType,
            "roleAssignmentId", roleAssignmentId,
            "taskId", taskId
        );
        inputVariables.putValue("taskAttributes", taskAttributes);
        inputVariables.putValue("taskType", taskType);
        inputVariables.putValue("caseData", caseData);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        resultsMatch(dmnDecisionTableResult.getResultList(), expectation);
    }

    private void resultsMatch(List<Map<String, Object>> results, List<Map<String, Object>> expectation) {
        assertThat(results.size(), is(expectation.size()));

        for (int index = 0; index < expectation.size(); index++) {
            if (DUE_DATE_ORIGIN.equals(expectation.get(index).get("name"))) {
                assertEquals(
                    expectation.get(index).get("canReconfigure"),
                    results.get(index).get("canReconfigure")
                );
                assertTrue(validNow(
                    ZonedDateTime.parse(expectation.get(index).get("value").toString()),
                    ZonedDateTime.parse(results.get(index).get("value").toString())
                ));

            } else if (PRIORITY_DATE_ORIGIN_REF.equals(expectation.get(index).get("name"))) {
                assertEquals(
                    expectation.get(index).get("canReconfigure"),
                    results.get(index).get("canReconfigure")
                );
                assertTrue(LocalDate.parse(expectation.get(index).get("value").toString()).isEqual(
                    LocalDate.parse(results.get(index).get("value").toString()))
                        || LocalDate.parse(expectation.get(index).get("value").toString()).isAfter(
                            LocalDate.parse(results.get(index).get("value").toString()))
                );

            } else {
                assertThat(results.get(index), is(expectation.get(index)));
            }
        }
    }

    private boolean validNow(ZonedDateTime expected, ZonedDateTime result) {
        ZonedDateTime now = ZonedDateTime.now();
        return result != null
            && (expected.isEqual(result) || expected.isBefore(result))
            && (now.isEqual(result) || now.isAfter(result));
    }
}
