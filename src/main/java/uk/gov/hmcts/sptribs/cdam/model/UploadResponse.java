package uk.gov.hmcts.sptribs.cdam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public class UploadResponse {

    private List<Document> documents;

    public List<Document> getDocuments() {
        return documents;
    }
}
