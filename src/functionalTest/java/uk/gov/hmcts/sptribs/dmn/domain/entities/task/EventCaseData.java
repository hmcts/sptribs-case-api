package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import lombok.Getter;

@Getter
public class EventCaseData {
    private final String eventId;
    private final String template;

    public EventCaseData(String eventId, String template) {
        this.eventId = eventId;
        this.template = template;
    }
}
