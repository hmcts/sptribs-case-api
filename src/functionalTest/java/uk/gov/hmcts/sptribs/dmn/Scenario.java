package uk.gov.hmcts.sptribs.dmn;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class Scenario {

    private final Map<String, Object> scenarioMapValues;
    private final String scenarioSource;
    private final Map<String, Object> beforeClauseValues;
    private final List<Map<String, Object>> testClauseValues;
    private final Map<String, Object> postRoleAssignmentClauseValues;
    private final Map<String, Object> updateCaseClauseValues;
    private final String jurisdiction;
    private final String caseType;

    private final List<String> taskIds;
    private String assigneeId;
    private final Map<String, String> caseIdMap;
    private final Set<Map<String, Object>> searchMap;

    public Scenario(Map<String, Object> scenarioMapValues,
                    String scenarioSource, Map<String, Object> beforeClauseValues,
                    List<Map<String, Object>> testClauseValues,
                    Map<String, Object> postRoleAssignmentClauseValues,
                    Map<String, Object> updateCaseClauseValues,
                    String jurisdiction,
                    String caseType,
                    List<String> taskIds,
                    String assigneeId,
                    Map<String, String> caseIdMap,
                    Set<Map<String, Object>> searchMap
    ) {
        this.scenarioMapValues = scenarioMapValues;
        this.scenarioSource = scenarioSource;
        this.beforeClauseValues = beforeClauseValues;
        this.testClauseValues = testClauseValues;
        this.postRoleAssignmentClauseValues = postRoleAssignmentClauseValues;
        this.updateCaseClauseValues = updateCaseClauseValues;
        this.jurisdiction = jurisdiction;
        this.caseType = caseType;
        this.taskIds = taskIds;
        this.assigneeId = assigneeId;
        this.caseIdMap = caseIdMap;
        this.searchMap = searchMap;
    }

    public void addAssignedCaseId(String key, String caseId) {
        caseIdMap.put(key, caseId);
    }

    public String getAssignedCaseId(String key) {
        return caseIdMap.get(key);
    }

    public Map<String, String> getAssignedCaseIdMap() {
        return caseIdMap;
    }

    public String getCaseType() {
        return caseType;
    }

    public Map<String, Object> getBeforeClauseValues() {
        return beforeClauseValues;
    }

    public List<Map<String, Object>> getTestClauseValues() {
        return testClauseValues;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public Map<String, Object> getPostRoleAssignmentClauseValues() {
        return postRoleAssignmentClauseValues;
    }

    public void addSearchMap(Map<String, Object> map) {
        searchMap.add(map);
    }

    public Set<Map<String, Object>> getSearchMap() {
        return searchMap;
    }

    public Map<String, Object> getUpdateCaseClauseValues() {
        return updateCaseClauseValues;
    }

    public void addTaskId(String taskId) {
        assertNotNull(taskId);
        taskIds.add(taskId);
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }
}
