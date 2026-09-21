package uk.gov.hmcts.sptribs.common.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.MapperFeature.INFER_BUILDER_TYPE_BINDINGS;
import static com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer.INSTANCE;

@Configuration
public class JacksonConfiguration {

    @Primary
    @Bean
    public ObjectMapper getMapper() {
        final ObjectMapper mapper = JsonMapper.builder()
            .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .enable(INFER_BUILDER_TYPE_BINDINGS)
            .serializationInclusion(NON_NULL)
            .build();

        final SimpleModule deserialization = new SimpleModule();
        deserialization.addDeserializer(HasRole.class, new HasRoleDeserializer());
        deserialization.addDeserializer(InternalHealth.class, new InternalHealthDeserializer());
        deserialization.addDeserializer(DynamicList.class, new DynamicListDeserializer());
        deserialization.addDeserializer(DynamicMultiSelectList.class, new DynamicMultiSelectListDeserializer());
        deserialization.addDeserializer(Document.class, new DocumentDeserializer());
        deserialization.addDeserializer(CICDocument.class, new CicDocumentDeserializer());
        mapper.registerModule(deserialization);

        final JavaTimeModule datetime = new JavaTimeModule();
        datetime.addSerializer(INSTANCE);
        mapper.registerModule(datetime);
        mapper.registerModule(new ParameterNamesModule(PROPERTIES));
        mapper.enable(INCLUDE_SOURCE_IN_LOCATION);

        return mapper;
    }

    private static final class DynamicListDeserializer
        extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<DynamicList> {

        private DynamicListDeserializer() {
            super(DynamicList.class);
        }

        @Override
        public DynamicList deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            DynamicListElement value = node.hasNonNull("value")
                ? mapper.treeToValue(node.get("value"), DynamicListElement.class)
                : null;
            return new DynamicList(value, dynamicListElements(node.get("list_items"), mapper));
        }
    }

    private static final class DynamicMultiSelectListDeserializer
        extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<DynamicMultiSelectList> {

        private DynamicMultiSelectListDeserializer() {
            super(DynamicMultiSelectList.class);
        }

        @Override
        public DynamicMultiSelectList deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            return new DynamicMultiSelectList(
                dynamicListElements(node.get("value"), mapper),
                dynamicListElements(node.get("list_items"), mapper)
            );
        }
    }

    private static final class DocumentDeserializer
        extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<Document> {

        private DocumentDeserializer() {
            super(Document.class);
        }

        @Override
        public Document deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
            LocalDateTime uploadTimestamp = node.hasNonNull("upload_timestamp")
                ? ((ObjectMapper) parser.getCodec()).treeToValue(node.get("upload_timestamp"), LocalDateTime.class)
                : null;
            return new Document(
                textValue(node, "document_url"),
                textValue(node, "document_filename"),
                textValue(node, "document_binary_url"),
                textValue(node, "category_id"),
                uploadTimestamp,
                textValue(node, "document_hash")
            );
        }

    }

    private static final class CicDocumentDeserializer
        extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<CICDocument> {

        private CicDocumentDeserializer() {
            super(CICDocument.class);
        }

        @Override
        public CICDocument deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            Document document = node.hasNonNull("documentLink")
                ? mapper.treeToValue(node.get("documentLink"), Document.class)
                : null;
            return new CICDocument(textValue(node, "documentEmailContent"), document);
        }
    }

    private static List<DynamicListElement> dynamicListElements(JsonNode node, ObjectMapper mapper)
        throws com.fasterxml.jackson.core.JsonProcessingException {
        if (node == null || node.isNull()) {
            return null;
        }
        List<DynamicListElement> elements = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                elements.add(mapper.treeToValue(item, DynamicListElement.class));
            }
        }
        return elements;
    }

    private static String textValue(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

}
