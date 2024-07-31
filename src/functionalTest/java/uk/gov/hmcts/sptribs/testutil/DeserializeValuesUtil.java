package uk.gov.hmcts.sptribs.testutil;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class DeserializeValuesUtil {

    private final MapValueExpander mapValueExpander;

    public DeserializeValuesUtil(MapValueExpander mapValueExpander) {
        this.mapValueExpander = mapValueExpander;
    }

    public Map<String, Object> deserializeStringWithExpandedValues(String source,
                                                                   Map<String, String> additionalValues)
        throws IOException {
        Map<String, Object> data = MapSerializer.deserialize(source);
        //Map Value Expander will mutate the "data" source

        mapValueExpander.expandValues(data, additionalValues);
        return data;
    }

    public Map<String, Object> expandMapValues(Map<String, Object> source,
                                               Map<String, String> additionalValues) {
        //Map Value Expander will mutate the "source"
        mapValueExpander.expandValues(source, additionalValues);
        return source;
    }
}
