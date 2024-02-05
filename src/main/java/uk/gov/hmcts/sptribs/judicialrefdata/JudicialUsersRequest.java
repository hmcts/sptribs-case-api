package uk.gov.hmcts.sptribs.judicialrefdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudicialUsersRequest {

    @JsonProperty("ccdServiceName")
    private String ccdServiceName;
}
