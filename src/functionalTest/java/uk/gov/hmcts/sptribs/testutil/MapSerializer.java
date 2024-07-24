package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapSerializer {

    private static ObjectMapper MAPPER = new ObjectMapper();

    public static void setObjectMapper(
        ObjectMapper objectMapper
    ) {
        MAPPER = objectMapper;
    }

    public static Map<String, Object> deserialize(String source) throws IOException {

        return MAPPER.readValue(
            source,
            new TypeReference<>() {
            }
        );
    }

    public static String sortCollectionElement(String source, String elementNameToSort, Comparator<JsonNode> comparator)
        throws IOException {
        JsonNode node = MAPPER.readTree(source);
        ObjectNode objectNode = node.deepCopy();

        if (node.get(elementNameToSort) instanceof ArrayNode) {
            ArrayNode list = (ArrayNode) node.get(elementNameToSort);

            List<JsonNode> sortList = new ArrayList<>();
            list.elements().forEachRemaining(sortList::add);
            Collections.sort(sortList, comparator);

            ArrayNode array = MAPPER.createArrayNode();
            sortList.forEach(array::add);
            objectNode.set(elementNameToSort, array);

            return MAPPER.writeValueAsString(objectNode);
        }
        return source;
    }

    public static String serialize(Map<String, Object> map) throws IOException {

        return MAPPER.writeValueAsString(map);
    }
}
