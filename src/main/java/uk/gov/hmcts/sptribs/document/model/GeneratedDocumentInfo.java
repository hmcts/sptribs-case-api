package uk.gov.hmcts.sptribs.document.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneratedDocumentInfo {
    private String url;
    private String mimeType;
    private String createdOn;
    private String hashToken;
    private String binaryUrl;
}
