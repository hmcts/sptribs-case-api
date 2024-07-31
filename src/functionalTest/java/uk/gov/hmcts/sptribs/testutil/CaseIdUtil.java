package uk.gov.hmcts.sptribs.testutil;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.Expectation;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CaseIdUtil {

    private static final String ASSIGNED_CASE_ID_KEY_FIELD = "caseIdKey";
    private static final String DEFAULT_ASSIGNED_CASE_ID_KEY = "defaultCaseId";

//    public static String extractAssignedCaseIdOrDefault(Map<String, Object> values, TestScenario scenario) {
//
//        String assignedCaseIdKey = extract(values, ASSIGNED_CASE_ID_KEY_FIELD);
//
//        if (StringUtils.isNotEmpty(assignedCaseIdKey)) {
//            String assignedCaseId = scenario.getAssignedCaseId(assignedCaseIdKey);
//            if (StringUtils.isNotEmpty(assignedCaseId)) {
//                return assignedCaseId;
//            } else {
//                throw new IllegalStateException("Case Id not found for '" + assignedCaseIdKey + "'");
//            }
//        }
//
//        return scenario.getAssignedCaseId(DEFAULT_ASSIGNED_CASE_ID_KEY);
//    }

    public static List<String> extractAllAssignedCaseIdOrDefault(Expectation values, TestScenario scenario) {

        String assignedCaseIdKey = extract(values, ASSIGNED_CASE_ID_KEY_FIELD);

        // TODO - none of the json files have caseIdKey assigned. how is it used? do we need it?
        if (StringUtils.isNotEmpty(assignedCaseIdKey)) {
            if (assignedCaseIdKey.contains(",")) {
                String[] keys = assignedCaseIdKey.split(",");
                List<String> caseIds = new ArrayList<>();
                for (String key : keys) {
                    caseIds.add(scenario.getAssignedCaseId(key));
                }
                return caseIds;
            } else {
                String assignedCaseId = scenario.getAssignedCaseId(assignedCaseIdKey);
                if (StringUtils.isNotEmpty(assignedCaseId)) {
                    return Collections.singletonList(assignedCaseId);
                } else {
                    throw new IllegalStateException("Case Id not found for '" + assignedCaseIdKey + "'");
                }
            }
        }

        // Think it will just return the default ids
        return Collections.singletonList(scenario.getAssignedCaseId(DEFAULT_ASSIGNED_CASE_ID_KEY));
    }

//    public static void addAssignedCaseId(Map<String, Object> values, String caseId, TestScenario scenario) {
//        String assignedCaseIdKey = extract(values, ASSIGNED_CASE_ID_KEY_FIELD);
//        if (StringUtils.isNotEmpty(assignedCaseIdKey)) {
//            scenario.addAssignedCaseId(assignedCaseIdKey, caseId);
//        } else {
//            scenario.addAssignedCaseId(DEFAULT_ASSIGNED_CASE_ID_KEY, caseId);
//        }
//    }
}
