package uk.gov.hmcts.sptribs.consumer.bundle;

public class CaseBundle {
    private java.util.List<BundleFolder> folders;
    private String stitchStatus;
    private BundleDocument stitchedDocument;

    public java.util.List<BundleFolder> getFolders() {
        return folders;
    }

    public void setFolders(java.util.List<BundleFolder> folders) {
        this.folders = folders;
    }

    public String getStitchStatus() {
        return stitchStatus;
    }

    public void setStitchStatus(String stitchStatus) {
        this.stitchStatus = stitchStatus;
    }

    public BundleDocument getStitchedDocument() {
        return stitchedDocument;
    }

    public void setStitchedDocument(BundleDocument stitchedDocument) {
        this.stitchedDocument = stitchedDocument;
    }
}
