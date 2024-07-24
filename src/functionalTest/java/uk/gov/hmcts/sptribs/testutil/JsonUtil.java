package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
        // noop
    }

    public static void setObjectMapper(
        ObjectMapper objectMapper
    ) {
        MAPPER = objectMapper;
    }


    public static String toJsonString(Map<String, String> attributes) {
        String json = null;

        try {
            json = MAPPER.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static String toJsonString(List<String> attributes) {
        String json = null;

        try {
            json = MAPPER.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

}
