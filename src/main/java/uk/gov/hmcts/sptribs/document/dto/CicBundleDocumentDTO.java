package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Slf4j
public class CicBundleDocumentDTO {

    private String name;
    private String description;
    private int sortIndex;
    private CICDocument sourceDocument;

    public CicBundleDocumentDTO() {
        log.info("CicBundleDocumentDTO no args constructor");
    }

    public CicBundleDocumentDTO(String name, String description, int sortIndex, CICDocument sourceDocument) {
        this.name = name;
        this.description = description;
        this.sortIndex = sortIndex;
        this.sourceDocument = sourceDocument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public CICDocument getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(CICDocument sourceDocument) {
        this.sourceDocument = sourceDocument;
    }
}
