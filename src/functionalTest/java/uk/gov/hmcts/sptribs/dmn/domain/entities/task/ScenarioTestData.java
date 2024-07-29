package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.ArrayList;

public class ScenarioTestData {

    private final String credentials;
    private final ArrayList<EventMessage> eventMessages;

    private final ArrayList<Expectation> expectations;

    public ScenarioTestData(
        String credentials,
        ArrayList<EventMessage> eventMessages,
        ArrayList<Expectation> expectations
    ) {
        this.credentials = credentials;
        this.eventMessages = eventMessages;
        this.expectations = expectations;
    }
}
