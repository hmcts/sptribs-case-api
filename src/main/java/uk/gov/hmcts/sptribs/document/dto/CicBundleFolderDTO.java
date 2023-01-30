package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CicBundleFolderDTO {

    private String name;
    private List<CicValue<CicBundleDocumentDTO>> documents = new ArrayList<>();
    private List<CicValue<CicBundleFolderDTO>> folders = new ArrayList<>();
    private int sortIndex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CicValue<CicBundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CicValue<CicBundleDocumentDTO>> documents) {
        this.documents = documents;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<CicValue<CicBundleFolderDTO>> getFolders() {
        return folders;
    }

    public void setFolders(List<CicValue<CicBundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}
