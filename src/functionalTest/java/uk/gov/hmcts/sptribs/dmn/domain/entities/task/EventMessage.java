package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

public class EventMessage {

    private final String eventMessage;
    private final String template;
    private final String eventId;
    private final String newStateId;
    private final String cicCaseReferralTypeForWA;

    public EventMessage(String eventMessage, String template, String eventId, String newStateId, String cicCaseReferralTypeForWA) {
        this.eventMessage = eventMessage;
        this.template = template;
        this.eventId = eventId;
        this.newStateId = newStateId;
        this.cicCaseReferralTypeForWA = cicCaseReferralTypeForWA;
    }
}
