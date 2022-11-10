package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum CcdCaseType {

    ST_CIC("Criminal Injuries Compensation"),
    ST_CS("Care Standards"),
    ST_MH("Mental Health"),
    ST_PHL("Primary Health Lists"),
    ST_SEN("Special Educational Needs"),
    ST_DD("Disability Discrimination");

    @JsonValue
    private final String description;

    public static CcdCaseType fromString(String value) {
        for (CcdCaseType caseType : CcdCaseType.values()) {
            if (caseType.name().equalsIgnoreCase(value)) {
                return caseType;
            }
        }
        return null;
    }
}
