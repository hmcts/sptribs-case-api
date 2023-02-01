package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FullPanelHearing {

    @JsonProperty("No. It was a 'sit alone' hearing")
    NO("No. It was a 'sit alone' hearing"),

    @JsonProperty("Yes")
    YES("Yes");

    private final String label;
}
