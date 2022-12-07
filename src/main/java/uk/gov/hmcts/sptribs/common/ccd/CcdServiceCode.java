package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CcdServiceCode {

    ST_CIC("BBA2","CRIMINAL_INJURIES_COMPENSATION", CcdCaseType.CIC, "Special Tribunals: Criminal Injuries Compensation"),
    ST_CS("BCA1","CARE_STANDARDS",CcdCaseType.CS, "Special Tribunals: Care Standards"),
    ST_MH("BCA2","MENTAL_HEALTH", CcdCaseType.MH, "Special Tribunals: Mental Health"),
    ST_PHL("BCA3", "PRIMARY_HEALTH_LISTS", CcdCaseType.PHL, "Special Tribunals: Primary Health Lists"),
    ST_SEND("BCA4", "SPECIAL_EDUCATIONAL_NEEDS_AND_DISABILITIES", CcdCaseType.SEN, "Special Tribunals: Special Educational Needs"),
    ST_DD("BCA4", "SPECIAL_EDUCATIONAL_NEEDS_AND_DISABILITIES", CcdCaseType.DD, "Special Tribunals: Disability Discrimination");

    @JsonValue
    private final String ccdServiceId;
    private final String ccdServiceName;
    private final CcdCaseType caseType;
    private final String ccdServiceDescription;

    public static CcdServiceCode fromString(String value) {
        for (CcdServiceCode ccdCServiceCode : CcdServiceCode.values()) {
            if (ccdCServiceCode.name().equalsIgnoreCase(value)) {
                return ccdCServiceCode;
            }
        }
        return null;
    }

}
