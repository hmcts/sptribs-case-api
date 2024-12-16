package uk.gov.hmcts.sptribs.cdam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public class Document {

    public Classification classification;
    public long size;
    public String mimeType;
    public String originalDocumentName;
    public Date createdOn;
    public Date modifiedOn;
    public String createdBy;
    public String lastModifiedBy;
    public Date ttl;
    public String hashToken;
    public Map<String, String> metadata;

    @JsonProperty("_links")
    public Links links;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        public DocumentLink self;
        public DocumentLink binary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentLink {
        public String href;
    }
}
