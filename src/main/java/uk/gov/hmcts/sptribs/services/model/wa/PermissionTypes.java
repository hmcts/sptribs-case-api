package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PermissionTypes {
    READ("Read"), REFER("Refer"), OWN("Own"), MANAGE("Manage"), EXECUTE("Execute"),
    CANCEL("Cancel"), COMPLETE("Complete"), COMPLETE_OWN("CompleteOwn"),
    CANCEL_OWN("CancelOwn"), CLAIM("Claim"), UNCLAIM("Unclaim"), ASSIGN("Assign"),
    UNASSIGN("Unassign"), UNCLAIM_ASSIGN("UnclaimAssign"),
    UNASSIGN_CLAIM("UnassignClaim"), UNASSIGN_ASSIGN("UnassignAssign");

    @JsonValue
    private final String value;
    PermissionTypes(String value) { this.value = value; }
}
