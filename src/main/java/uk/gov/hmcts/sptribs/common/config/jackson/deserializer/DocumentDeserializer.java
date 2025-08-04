package uk.gov.hmcts.sptribs.common.config.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.ccd.sdk.type.Document;

import java.io.IOException;

public class DocumentDeserializer extends JsonDeserializer<Document> {

    @Override
    public Document deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        String url = node.has("document_url") && !node.get("document_url").isNull() ? node.get("document_url").asText() : null;
        String filename = node.has("document_filename") && !node.get("document_filename").isNull() ? node.get("document_filename").asText() : ""; // Ensure non-null
        String binaryUrl = node.has("document_binary_url") && !node.get("document_binary_url").isNull() ? node.get("document_binary_url").asText() : null;
        String categoryId = node.has("category_id") && !node.get("category_id").isNull() ? node.get("category_id").asText() : null;

        return new Document(url, filename, binaryUrl, categoryId);
    }
}
