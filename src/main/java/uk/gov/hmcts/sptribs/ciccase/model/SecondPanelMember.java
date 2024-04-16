package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecondPanelMember {

    @JsonProperty("Tribunal Judge")
    TRIBUNAL_JUDGE("Tribunal Judge"),

    @JsonProperty("Medical Member")
    MEDICAL_MEMBER("Medical Member"),

    @JsonProperty("Lay Member")
    LAY_MEMBER("Lay Member");

    private final String label;
}
