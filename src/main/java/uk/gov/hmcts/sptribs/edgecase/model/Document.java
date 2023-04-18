package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.edgecase.model.access.DefaultAccess;

@Value
@Builder(toBuilder = true)
public class Document {

    @CCD(
        label = "Document URL",
        access = {DefaultAccess.class}
    )
    String documentUrl;

    @CCD(
        label = "Document Binary URL",
        access = {DefaultAccess.class}
    )
    String documentBinaryUrl;

    @CCD(
        label = "Document File Name",
        access = {DefaultAccess.class}
    )
    String documentFileName;

    @CCD(
        label = "Document hash",
        access = {DefaultAccess.class}
    )
    String documentHash;

    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_binary_url") String documentBinaryUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
    }
}
