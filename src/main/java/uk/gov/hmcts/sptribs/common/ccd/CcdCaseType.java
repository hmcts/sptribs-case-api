package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum CcdCaseType {

    CIC("Criminal Injuries Compensation"),
    CS("Care Standards"),
    MH("Mental Health"),
    PHL("Primary Health Lists"),
    SEN("Special Educational Needs"),
    DD("Disability Discrimination");

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
