package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CicBundleDTO {

    private String id;
    private String title;
    @Size(max = 255, message = CommonConstants.BUNDLE_DESCRIPTION_FIELD_LENGTH_ERROR_MSG)
    private String description;
    private String eligibleForStitching;
    private String eligibleForCloning;
    private CICDocument stitchedDocument;
    private List<CicValue<CicBundleDocumentDTO>> documents = new LinkedList<>();
    private List<CicValue<CicBundleFolderDTO>> folders = new LinkedList<>();

    @Size(min = 2, max = 50, message = CommonConstants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG)
    @Pattern(regexp = "^[-._A-Za-z0-9]*$")
    private String fileName;
    private String fileNameIdentifier;
    private String coverpageTemplate;
    @JsonIgnore
    private JsonNode coverpageTemplateData;
    private CicBoolean hasTableOfContents;
    private CicBoolean hasCoversheets;
    private CicBoolean hasFolderCoversheets;
    private String stitchStatus;
    private CicBundlePaginationStyle paginationStyle = CicBundlePaginationStyle.off;
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CicBoolean enableEmailNotification;
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

    public String isEligibleForStitching() {
        return eligibleForStitching;
    }

    public void setEligibleForStitching(String eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching;
    }

    public String getEligibleForStitching() {
        return eligibleForStitching;
    }

    @JsonIgnore
    public boolean getEligibleForStitchingAsBoolean() {
        return eligibleForStitching != null && eligibleForStitching.equalsIgnoreCase("yes");
    }

    @JsonIgnore
    public void setEligibleForStitchingAsBoolean(boolean eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching ? "yes" : "no";
    }

    public void setEligibleForCloning(String eligibleForCloning) {
        this.eligibleForCloning = eligibleForCloning;
    }

    public String getEligibleForCloning() {
        return eligibleForCloning;
    }

    @JsonIgnore
    public boolean getEligibleForCloningAsBoolean() {
        return eligibleForCloning != null && eligibleForCloning.equalsIgnoreCase("yes");
    }

    @JsonIgnore
    public void setEligibleForCloningAsBoolean(boolean eligibleForCloning) {
        this.eligibleForCloning = eligibleForCloning ? "yes" : "no";
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

    public void setFolders(List<CicValue<CicBundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameIdentifier() {
        return fileNameIdentifier;
    }

    public void setFileNameIdentifier(String fileNameIdentifier) {
        this.fileNameIdentifier = fileNameIdentifier;
    }

    public String getCoverpageTemplate() {
        return coverpageTemplate;
    }

    public void setCoverpageTemplate(String coverpageTemplate) {
        this.coverpageTemplate = coverpageTemplate;
    }

    public JsonNode getCoverpageTemplateData() {
        return coverpageTemplateData;
    }

    public void setCoverpageTemplateData(JsonNode coverpageTemplateData) {
        this.coverpageTemplateData = coverpageTemplateData;
    }

    public CicBoolean getHasTableOfContents() {
        return hasTableOfContents;
    }

    public void setHasTableOfContents(CicBoolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents;
    }

    public CicBoolean getHasCoversheets() {
        return hasCoversheets;
    }

    public void setHasCoversheets(CicBoolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets;
    }

    public String getStitchStatus() {
        return stitchStatus;
    }

    public void setStitchStatus(String stitchStatus) {
        this.stitchStatus = stitchStatus;
    }

    public CicBoolean getHasFolderCoversheets() {
        return hasFolderCoversheets;
    }

    public void setHasFolderCoversheets(CicBoolean hasFolderCoversheets) {
        this.hasFolderCoversheets = hasFolderCoversheets;
    }

    public void setHasCoversheetsAsBoolean(boolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets ? CicBoolean.Yes : CicBoolean.No;
    }

    public void setHasTableOfContentsAsBoolean(boolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents ? CicBoolean.Yes : CicBoolean.No;
    }

    public void setHasFolderCoversheetsAsBoolean(boolean hasFolderCoversheets) {
        this.hasFolderCoversheets = hasFolderCoversheets ? CicBoolean.Yes : CicBoolean.No;
    }

    public CicBundlePaginationStyle getPaginationStyle() {
        return paginationStyle;
    }

    public void setPaginationStyle(CicBundlePaginationStyle paginationStyle) {
        this.paginationStyle = paginationStyle;
    }

    public PageNumberFormat getPageNumberFormat() {
        return pageNumberFormat;
    }

    public void setPageNumberFormat(PageNumberFormat pageNumberFormat) {
        this.pageNumberFormat = pageNumberFormat;
    }

    public String getStitchingFailureMessage() {
        return stitchingFailureMessage;
    }

    public void setStitchingFailureMessage(String stitchingFailureMessage) {
        this.stitchingFailureMessage = stitchingFailureMessage;

    }

    public CicBoolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(CicBoolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    @JsonIgnore
    public void setEnableEmailNotificationAsBoolean(Boolean enableEmailNotification) {
        if (enableEmailNotification == null) {
            this.enableEmailNotification = null;
        } else {
            this.enableEmailNotification = enableEmailNotification ? CicBoolean.Yes : CicBoolean.No;
        }
    }

    @JsonIgnore
    public Boolean getEnableEmailNotificationAsBoolean() {
        return enableEmailNotification != null ? enableEmailNotification == CicBoolean.Yes : null;
    }

    public DocumentImage getDocumentImage() {
        return documentImage;
    }

    public void setDocumentImage(DocumentImage documentImage) {
        this.documentImage = documentImage;
    }
}
