package uk.gov.hmcts.sptribs.e2e;

public enum CaseState {

    AwaitingHearing("Awaiting hearing"),
    AwaitingOutcome("Awaiting outcome"),
    CaseManagement("Case management"),
    CaseClosed("Case closed"),
    CaseStayed("Case stayed"),
    Submitted("Submitted");

    public final String label;

    CaseState(String label) {
        this.label = label;
    }
}
