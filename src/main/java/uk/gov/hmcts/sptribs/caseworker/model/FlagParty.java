package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccess;

@Getter
@AllArgsConstructor
public enum FlagParty {

    @CCD(
        access = {DefaultStateAccess.class}

    )
    SUBJECT("subject"),
    @CCD(
        access = {DefaultStateAccess.class}

    )
    APPLICANT("applicant"),
    @CCD(
        access = {DefaultStateAccess.class}

    )
    REPRESENTATIVE("representative");

    private final String name;

    public boolean isSubject() {
        return SUBJECT.name().equalsIgnoreCase(this.name());
    }

    public boolean isRepresentativeCIC() {
        return REPRESENTATIVE.name().equalsIgnoreCase(this.name());
    }

    public boolean isApplicantCIC() {
        return APPLICANT.name().equalsIgnoreCase(this.name());
    }
}
