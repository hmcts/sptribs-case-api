package uk.gov.hmcts.sptribs.cdam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResponse {

    private List<Document> documents;

    public List<Document> getDocuments() {
        return documents;
    }
}
