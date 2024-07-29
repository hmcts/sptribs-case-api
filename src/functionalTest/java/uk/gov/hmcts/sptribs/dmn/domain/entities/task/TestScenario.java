package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.ArrayList;

public class TestScenario {
    private final String description;
    private final String jurisdiction;
    private final String caseType;
    private final ArrayList<EventCaseData> requiredCaseData;
    private final ArrayList<EventCaseData> updateCaseData;
    private final ArrayList<ScenarioTestData> tests;

    public TestScenario(String description, String jurisdiction, String caseType, ArrayList<EventCaseData> requiredCaseData, ArrayList<EventCaseData> updateCaseData, ArrayList<ScenarioTestData> tests) {
        this.description = description;
        this.jurisdiction = jurisdiction;
        this.caseType = caseType;
        this.requiredCaseData = requiredCaseData;
        this.updateCaseData = updateCaseData;
        this.tests = tests;
    }
}
