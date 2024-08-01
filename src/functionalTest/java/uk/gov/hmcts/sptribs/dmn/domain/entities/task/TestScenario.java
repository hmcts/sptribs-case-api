package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import lombok.Getter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Getter
public class TestScenario {
    private final String description;
    private final String jurisdiction;
    private final String caseType;
    private final List<EventCaseData> requiredCaseData;
    private final List<EventCaseData> updateCaseData;
    private final ScenarioTestData test;
    private final String requiredCredentials;
    private final String updateCredentials;
    private final Map<String, String> caseIdMap;
    private final List<String> taskIds;

    public TestScenario(
            String description,
            String jurisdiction,
            String caseType,
            List<EventCaseData> requiredCaseData,
            List<EventCaseData> updateCaseData,
            ScenarioTestData test,
            String requiredCredentials,
            String updateCredentials,
            Map<String, String> caseIdMap, List<String> taskIds) {
        this.description = description;
        this.jurisdiction = jurisdiction;
        this.caseType = caseType;
        this.requiredCaseData = requiredCaseData;
        this.updateCaseData = updateCaseData;
        this.test = test;
        this.requiredCredentials = requiredCredentials;
        this.updateCredentials = updateCredentials;
        this.caseIdMap = caseIdMap;
        this.taskIds = taskIds;
    }

    public void addAssignedCaseId(String key, String caseId) {
        caseIdMap.put(key, caseId);
    }

    public void addTaskId(String taskId) {
        assertNotNull(taskId);
        taskIds.add(taskId);
    }
}
