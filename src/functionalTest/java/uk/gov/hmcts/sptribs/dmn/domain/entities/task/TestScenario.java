package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import lombok.Getter;

import java.util.List;

@Getter
public class TestScenario {
    private final String description;
    private final String jurisdiction;
    private final String caseType;
    private final List<EventCaseData> requiredCaseData;
    private final List<EventCaseData> updateCaseData;
    private final List<ScenarioTestData> tests;
    private final String requiredCredentials;
    private final String updateCredentials;

    public TestScenario(
        String description,
        String jurisdiction,
        String caseType,
        List<EventCaseData> requiredCaseData,
        List<EventCaseData> updateCaseData,
        List<ScenarioTestData> tests,
        String requiredCredentials,
        String updateCredentials
    ) {
        this.description = description;
        this.jurisdiction = jurisdiction;
        this.caseType = caseType;
        this.requiredCaseData = requiredCaseData;
        this.updateCaseData = updateCaseData;
        this.tests = tests;
        this.requiredCredentials = requiredCredentials;
        this.updateCredentials = updateCredentials;
    }
}
