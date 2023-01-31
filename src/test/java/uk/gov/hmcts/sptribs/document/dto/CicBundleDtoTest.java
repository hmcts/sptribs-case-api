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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertNull(bundleDTO.getValue().getDescription());
        assertNotNull(bundleDTO.getValue().getStitchingFailureMessage());
        assertNotNull(bundleDTO.getValue().getDocuments());
        assertNotNull(bundleDTO.getValue().getFolders());
        assertNull(bundleDTO.getValue().getId());
        assertNotNull(bundleDTO.getValue().getDocumentImage());
        assertNotNull(bundleDTO.getValue().getPageNumberFormat());
        assertNotNull(bundleDTO.getValue().getPaginationStyle());
        assertNull(bundleDTO.getValue().getStitchedDocument());
        CicValue<CicBundleDocumentDTO> bundleDocumentDTO = bundleDTO.getValue().getDocuments().get(0);
        assertEquals("firstBundle", bundleDocumentDTO.getValue().getName());
        assertEquals("First description", bundleDocumentDTO.getValue().getDescription());
        assertEquals(1, bundleDocumentDTO.getValue().getSortIndex());
        assertNull(bundleDocumentDTO.getValue().getSourceDocument());

        CicValue<CicBundleFolderDTO> bundleFolderDTO = bundleDTO.getValue().getFolders().get(0);
        assertEquals("firstFolder", bundleFolderDTO.getValue().getName());
        assertNull(bundleFolderDTO.getValue().getFolders());
        assertEquals(1, bundleDocumentDTO.getValue().getSortIndex());
        assertNull(bundleFolderDTO.getValue().getDocuments());

        assertEquals(10, bundleDTO.getValue().getDocumentImage().getCoordinateX().intValue());
        assertEquals(20, bundleDTO.getValue().getDocumentImage().getCoordinateY().intValue());
        assertEquals("12345", bundleDTO.getValue().getDocumentImage().getDocmosisAssetId());
        assertNull(bundleDTO.getValue().getDocumentImage().getImageRendering());
        assertNull(bundleDTO.getValue().getDocumentImage().getImageRenderingLocation());
    }
}
