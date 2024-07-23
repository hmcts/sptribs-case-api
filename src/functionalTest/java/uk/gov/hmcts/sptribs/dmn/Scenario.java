package uk.gov.hmcts.sptribs.dmn;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Getter
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

    @Autowired
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
}
