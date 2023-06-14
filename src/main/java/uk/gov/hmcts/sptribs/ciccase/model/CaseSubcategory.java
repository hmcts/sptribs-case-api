package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CaseSubcategory implements HasLabel {
    @JsonProperty("Fatal")
    FATAL("Fatal"),

    @JsonProperty("MedicalReOpening")
    MEDICAL_REOPENING("Medical Re-opening"),

    @JsonProperty("Minor")
    MINOR("Minor"),

    @JsonProperty("Paragraph26")
    PARAGRAPH_26("Paragraph 26"),

    @JsonProperty("sexualAbuse")
    SEXUAL_ABUSE("Sexual Abuse"),

    @JsonProperty("SpecialJurisdiction")
    SPECIAL_JURISDICTION("Special Jurisdiction"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
