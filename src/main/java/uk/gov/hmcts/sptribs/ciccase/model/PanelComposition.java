package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PanelComposition {

    @JsonProperty("Panel 1")
    PANEL_1("Panel 1"),

    @JsonProperty("Panel 2")
    PANEL_2("Panel 2"),

    @JsonProperty("Panel 3")
    PANEL_3("Panel 3");

    private final String label;
}
