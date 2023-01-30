package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CicBundleDtoTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile = new File(ClassLoader.getSystemResource("case.json").getPath());
    private final JavaType type = mapper.getTypeFactory().constructParametricType(CicValue.class, CicBundleDTO.class);

    @Test
    public void testDeserialization() throws IOException {
        JsonNode root = mapper.readTree(jsonFile);
        ArrayNode bundles = (ArrayNode) root.path("case_details").path("case_data").path("caseBundles");
        JsonNode firstBundle = bundles.get(0);

        CicValue<CicBundleDTO> bundleDTO = mapper.readValue(mapper.treeAsTokens(firstBundle), type);

        assertEquals("Bundle Title", bundleDTO.getValue().getTitle());
    }
}
