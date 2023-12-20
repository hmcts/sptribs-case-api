package uk.gov.hmcts.sptribs.common.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CcdJurisdiction {

    CARE_STANDARDS("ST_CS", "CS"),
    CRIMINAL_INJURIES_COMPENSATION("ST_CIC", "CIC"),
    MENTAL_HEALTH("ST_MH", "MH"),
    PRIMARY_HEALTH_LISTS("ST_PHL", "PHL"),
    SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION("ST_SEND", "SEND");


    private final String jurisdictionId;

    private final String jurisdictionName;
}
