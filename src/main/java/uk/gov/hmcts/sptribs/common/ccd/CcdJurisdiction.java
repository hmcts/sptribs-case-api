package uk.gov.hmcts.sptribs.common.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CcdJurisdiction {

    CRIMINAL_INJURIES_COMPENSATION("ST_CIC", "CIC");

    private final String jurisdictionId;
    private final String jurisdictionName;
}
