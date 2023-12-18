package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CcdServiceCode {

    ST_CIC("ST_CIC","BBA2","CRIMINAL_INJURIES_COMPENSATION", CcdCaseType.CIC, "Special Tribunals: Criminal Injuries Compensation");

    private final String ccdServiceAcronym;
    @JsonValue
    private final String ccdServiceId;
    private final String ccdServiceName;
    private final CcdCaseType caseType;
    private final String ccdServiceDescription;
}
