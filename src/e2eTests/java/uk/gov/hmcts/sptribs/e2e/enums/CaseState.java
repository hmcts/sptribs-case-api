package uk.gov.hmcts.sptribs.e2e.enums;

public enum CaseState {

    AwaitingHearing("Awaiting hearing"),
    AwaitingOutcome("Awaiting outcome"),
    CaseClosed("Case closed"),
    CaseManagement("Case management"),
    CaseStayed("Case stayed"),
    Submitted("Submitted");

    public final String label;

    CaseState(String label) {
        this.label = label;
    }
}
