package uk.gov.hmcts.sptribs.hearingvenue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Region {

    @JsonProperty("region_id")
    private String region_id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("welsh_description")
    private String welsh_description;

}
