package uk.gov.hmcts.sptribs.e2e.enums;

public enum CaseParties {
    Subject("Subject"),
    Applicant("Applicant"),
    Representative("Representative");

    public final String label;

    CaseParties(String label) {
        this.label = label;
    }
}
