package uk.gov.hmcts.sptribs.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;

import java.io.IOException;
import java.util.Arrays;

public class HasRoleDeserializer extends StdDeserializer<HasRole> {
    static final long serialVersionUID = 1L;

    public HasRoleDeserializer() {
        this(null);
    }

    protected HasRoleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public HasRole deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();

        return Arrays
            .stream(UserRoleCIC.values())
            .filter(r -> r.getRole().equals(node.asText()))
            .findFirst()
            .get();
    }
}
