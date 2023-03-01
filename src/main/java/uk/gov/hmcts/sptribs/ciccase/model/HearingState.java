package uk.gov.hmcts.sptribs.ciccase.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccess;

@RequiredArgsConstructor
@Getter
public enum HearingState {

    @CCD(
        label = "Listed",
        access = {DefaultStateAccess.class}
    )
    Listed("Listed"),

    @CCD(
        label = "Completed",
        access = {DefaultStateAccess.class}
    )
    Complete("Completed"),

    @CCD(
        label = "Cancelled",
        access = {DefaultStateAccess.class}
    )
    Cancelled("Cancelled"),

    @CCD(
        label = "Postponed",
        access = {DefaultStateAccess.class}
    )
    Postponed("Postponed");

    private final String name;
}

