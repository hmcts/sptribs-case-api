package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import lombok.Getter;

import java.util.List;

@Getter
public class ScenarioTestData {

    private final String credentials;
    private final List<EventMessage> eventMessages;

    private final List<Expectation> expectations;

    public ScenarioTestData(
        String credentials,
        List<EventMessage> eventMessages,
        List<Expectation> expectations
    ) {
        this.credentials = credentials;
        this.eventMessages = eventMessages;
        this.expectations = expectations;
    }
}
