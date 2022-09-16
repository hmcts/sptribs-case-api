package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum LinkCaseReason implements HasLabel {

    @JsonProperty("progressedAsPartOfLeadCase")
    PROGRESSED_AS_PART_OF_LEAD_CASE("Progressed as part of this lead case"),

    @JsonProperty("bail")
    BAIL("Bail"),

    @JsonProperty("caseConsolidated")
    CASE_CONSOLIDATED("Case consolidated"),

    @JsonProperty("familial")
    FAMILIAL("Familial"),

    @JsonProperty("guardian")
    GUARDIAN("Guardian"),

    @JsonProperty("homeOfficeRequest")
    HOME_OFFICE_REQUEST("Home Office request"),

    @JsonProperty("linkedForHearing")
    LINKED_FOR_HEARING("Linked for a hearing"),

    @JsonProperty("sharedEvidence")
    SHARED_EVIDENCE("Shared evidence"),

    @JsonProperty("otherAppealDecided")
    OTHER_APPEAL_DECIDED("Other appeal decided"),

    @JsonProperty("otherAppealPending")
    OTHER_APPEAL_PENDING("Other appeal pending"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;

}
