package uk.gov.hmcts.sptribs.e2e.enums;

public enum CaseState {
    CaseManagement("Case management"),
    AwaitingHearing("Awaiting hearing"),
    AwaitingOutcome("Awaiting outcome");

    public final String label;

    CaseState(String label) {
        this.label = label;
    }
}
