package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HearingFormat {

    @JsonProperty("Face to Face")
    FACE_TO_FACE("Face to face"),

    @JsonProperty("Hybrid")
    HYBRID("Hybrid"),

    @JsonProperty("Video")
    VIDEO("Video"),

    @JsonProperty("Telephone")
    TELEPHONE("Telephone"),

    @JsonProperty("Paper")
    PAPER("Paper");

    private final String label;
}
