package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocAssemblyResponse {
    private String renditionOutputLocation;

    public String getBinaryFilePath() {
        return renditionOutputLocation + "/binary";
    }
}
