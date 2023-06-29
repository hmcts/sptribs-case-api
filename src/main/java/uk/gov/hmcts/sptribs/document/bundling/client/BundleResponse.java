package uk.gov.hmcts.sptribs.document.bundling.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BundleResponse {
    @SuppressWarnings("PMD")
    private LinkedHashMap<String,Object> data;
    private List<String> errors;
    private List<String> warnings;
    private Integer documentTaskId;
}
