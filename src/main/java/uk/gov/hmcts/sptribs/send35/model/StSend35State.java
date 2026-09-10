package uk.gov.hmcts.sptribs.send35.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccess;

/**
 * States for the SEND35 case type.
 *
 * <p>Three states only. The prototype exists to prove the citizen journey reaches CCD and
 * that a caseworker can read the whole appeal back, so the progression stops at what makes
 * a case list and a set of tabs meaningful: an appeal arrives, the tribunal registers it
 * (SEND35 says within 10 working days), and eventually it closes.
 *
 * <p>Deliberately not {@code ciccase.model.State}: its hints reference CIC case fields and
 * several of its constants are CIC-domain. The name is prefixed because
 * {@code generateTypeScript} has a flat namespace and a second {@code State} collides.
 */
@RequiredArgsConstructor
@Getter
public enum StSend35State {

    @CCD(
        label = "Appeal submitted",
        access = {DefaultStateAccess.class}
    )
    Submitted("Submitted"),

    @CCD(
        label = "Appeal registered",
        access = {DefaultStateAccess.class}
    )
    Registered("Registered"),

    @CCD(
        label = "Appeal closed",
        access = {DefaultStateAccess.class}
    )
    Closed("Closed");

    private final String name;
}
