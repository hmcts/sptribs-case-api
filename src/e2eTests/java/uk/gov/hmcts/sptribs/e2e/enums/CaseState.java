package uk.gov.hmcts.sptribs.e2e.enums;

public enum CaseState {
    CaseManagement("Case management"),
    AwaitingHearing("Awaiting hearing"),
    AwaitingOutcome("Awaiting outcome"),
    CaseClosed("Case closed"),
    CaseStayed("Case stayed"),
    Submitted("Submitted");

    public final String label;

    CaseState(String label) {
        this.label = label;
    }
}
