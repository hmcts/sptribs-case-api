package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

import static uk.gov.hmcts.sptribs.ciccase.model.Gender.FEMALE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.MALE;

@Getter
@AllArgsConstructor
public enum MarriageFormation implements HasLabel {

    @JsonProperty("sameSexCouple")
    SAME_SEX_COUPLE("sameSexCouple", "Same-sex couple"),

    @JsonProperty("oppositeSexCouple")
    OPPOSITE_SEX_COUPLE("oppositeSexCouple", "Opposite-sex couple");

    private final String type;
    private final String label;

    public Gender getPartnerGender(Gender gender) {
        return this == SAME_SEX_COUPLE
            ? gender
            : gender == MALE
               ? FEMALE
                : MALE;
    }
}
