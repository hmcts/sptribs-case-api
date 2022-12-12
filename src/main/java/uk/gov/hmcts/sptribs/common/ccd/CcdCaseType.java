package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum CcdCaseType {

    CIC("CriminalInjuriesCompensation", "Criminal Injuries Compensation"),
    CS("CareStandards","Care Standards"),
    MH("MentalHealth", "Mental Health"),
    PHL("PrimaryHealthLists", "Primary Health Lists"),
    SEN("SpecialEducationalNeeds", "Special Educational Needs"),
    DD("DisabilityDiscrimination", "Disability Discrimination");

    @JsonValue
    private final String caseName;
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
