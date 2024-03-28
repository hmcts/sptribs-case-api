package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

import java.util.Base64;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Value
public class DocAssemblyRequest {
    String templateId;

    String outputType;

    JsonNode formPayload;

    String outputFilename;

    boolean secureDocStoreEnabled;

    String caseTypeId;

    String jurisdictionId;

    public static class DocAssemblyRequestBuilder {
        public DocAssemblyRequestBuilder templateId(String templateId) {
            this.templateId = Base64.getEncoder()
                .encodeToString(templateId.getBytes());
            return this;
        }
    }
}
