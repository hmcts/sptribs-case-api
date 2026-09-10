package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum CcdCaseType {

    CIC("CIC", "CriminalInjuriesCompensation", "Criminal Injuries Compensation"),

    SEND35("StSend35", "StSend35", "Appeal a decision about an education, health and care (EHC) plan");

    private final String caseTypeAcronym;
    @JsonValue
    private final String caseTypeName;
    private final String description;
}
