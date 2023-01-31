package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.Size;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CicBundleDTO {

    private String id;
    private String title;
    @Size(max = 255, message = CommonConstants.BUNDLE_DESCRIPTION_FIELD_LENGTH_ERROR_MSG)
    private String description;
    private CICDocument stitchedDocument;
    private List<CicValue<CicBundleDocumentDTO>> documents = new LinkedList<>();
    private List<CicValue<CicBundleFolderDTO>> folders = new LinkedList<>();
    private CicBundlePaginationStyle paginationStyle = CicBundlePaginationStyle.off;
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public CICDocument getStitchedDocument() {
        return stitchedDocument;
    }

    public void setStitchedDocument(CICDocument stitchedDocument) {
        this.stitchedDocument = stitchedDocument;
    }

    public List<CicValue<CicBundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CicValue<CicBundleDocumentDTO>> documents) {
        this.documents = documents;
    }

    public List<CicValue<CicBundleFolderDTO>> getFolders() {
        return folders;
    }

    public CicBundlePaginationStyle getPaginationStyle() {
        return paginationStyle;
    }

    public PageNumberFormat getPageNumberFormat() {
        return pageNumberFormat;
    }

    public String getStitchingFailureMessage() {
        return stitchingFailureMessage;
    }

    public void setStitchingFailureMessage(String stitchingFailureMessage) {
        this.stitchingFailureMessage = stitchingFailureMessage;

    }

    public DocumentImage getDocumentImage() {
        return documentImage;
    }

    public void setDocumentImage(DocumentImage documentImage) {
        this.documentImage = documentImage;
    }
}
