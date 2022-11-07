package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CcdServiceCode {

    CIC("BBA2","CRIMINAL_INJURIES_COMPENSATION", CcdCaseType.ST_CIC, "Special Tribunals: Criminal Injuries Compensation"),
    CS("BCA1","CARE_STANDARDS",CcdCaseType.ST_CS, "Special Tribunals: Care Standards"),
    MH("BCA2","MENTAL_HEALTH", CcdCaseType.ST_MH, "Special Tribunals: Mental Health"),
    PHL("BCA3", "PRIMARY_HEALTH_LISTS", CcdCaseType.ST_PHL, "Special Tribunals: Primary Health Lists"),
    SEN("BCA4", "SPECIAL_EDUCATIONAL_NEEDS_AND_DISABILITIES", CcdCaseType.ST_SEND, "Special Tribunals: Special Educational Needs"),
    DD("BCA4", "SPECIAL_EDUCATIONAL_NEEDS_AND_DISABILITIES", CcdCaseType.DD, "Special Tribunals: Disability Discrimination");

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
