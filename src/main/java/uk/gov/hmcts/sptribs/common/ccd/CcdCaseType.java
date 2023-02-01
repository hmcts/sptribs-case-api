package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum CcdCaseType {

    CIC("CIC", "CriminalInjuriesCompensation", "Criminal Injuries Compensation"),
    CS("CS", "CareStandards","Care Standards"),
    MH("MH", "MentalHealth", "Mental Health"),
    PHL("PHL", "PrimaryHealthLists", "Primary Health Lists"),
    SEN("SEN", "SpecialEducationalNeeds", "Special Educational Needs"),
    DD("DD", "DisabilityDiscrimination", "Disability Discrimination");


    private final String caseTypeAcronym;
    @JsonValue
    private final String caseTypeName;
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
