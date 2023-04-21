package uk.gov.hmcts.sptribs.document.bundling;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BundleTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile = new File(ClassLoader.getSystemResource("case.json").getPath());
    private final JavaType type = mapper.getTypeFactory().constructParametricType(ListValue.class, Bundle.class);

    @Test
    void testDeserialization() throws IOException {
        JsonNode root = mapper.readTree(jsonFile);
        ArrayNode bundles = (ArrayNode) root.path("case_details").path("case_data").path("caseBundles");
        JsonNode firstBundle = bundles.get(0);

        ListValue<Bundle> bundleDTO = mapper.readValue(mapper.treeAsTokens(firstBundle), type);

        assertThat(bundleDTO.getValue().getTitle()).isEqualTo("Bundle Title");
        assertThat(bundleDTO.getValue().getDescription()).isNull();
        assertThat(bundleDTO.getValue().getStitchingFailureMessage()).isNotNull();
        assertThat(bundleDTO.getValue().getDocuments()).isNotNull();
        assertThat(bundleDTO.getValue().getFolders()).isNotNull();
        assertThat(bundleDTO.getValue().getFolders().get(0).getValue().getFolders()).isNotNull();
        assertThat(bundleDTO.getValue().getFolders().get(0).getValue().getFolders().get(0).getValue().getFolders()).isNotNull();
        assertThat(bundleDTO.getValue().getId()).isNull();
        assertThat(bundleDTO.getValue().getDocumentImage()).isNotNull();
        assertThat(bundleDTO.getValue().getPageNumberFormat()).isNotNull();
        assertThat(bundleDTO.getValue().getPaginationStyle()).isNotNull();
        assertThat(bundleDTO.getValue().getStitchedDocument()).isNull();

        ListValue<BundleDocument> bundleDocumentDTO = bundleDTO.getValue().getDocuments().get(0);
        assertDocument(bundleDocumentDTO);

        ListValue<BundleFolder> bundleFolderDTO = bundleDTO.getValue().getFolders().get(0);
        assertFolder(bundleFolderDTO, bundleDocumentDTO);

        ListValue<BundleSubFolder> bundleSubFolderDTO = bundleFolderDTO.getValue().getFolders().get(0);
        assertSubFolder(bundleSubFolderDTO);
        assertSubFolder2(bundleSubFolderDTO);
        assertDocumentImage(bundleDTO);

    }

    private void assertDocument(ListValue<BundleDocument> bundleDocumentDTO) {
        assertThat(bundleDocumentDTO.getValue().getName()).isEqualTo("firstBundle");
        assertThat(bundleDocumentDTO.getValue().getDescription()).isEqualTo("First description");
        assertThat(bundleDocumentDTO.getValue().getSortIndex()).isEqualTo(1);
        assertThat(bundleDocumentDTO.getValue().getSourceDocument()).isNull();
    }

    private void assertFolder(ListValue<BundleFolder> bundleFolderDTO, ListValue<BundleDocument> bundleDocumentDTO) {
        assertThat(bundleFolderDTO.getValue().getName()).isEqualTo("firstFolder");
        assertThat(bundleFolderDTO.getValue().getFolders()).isNotNull();
        assertThat(bundleDocumentDTO.getValue().getSortIndex()).isEqualTo(1);
        assertThat(bundleFolderDTO.getValue().getDocuments()).isNull();
    }

    private void assertSubFolder(ListValue<BundleSubFolder> bundleSubFolderDTO) {
        assertThat(bundleSubFolderDTO.getValue().getName()).isEqualTo("firstSubFolder");
        assertThat(bundleSubFolderDTO.getValue().getFolders()).isNotNull();
        assertThat(bundleSubFolderDTO.getValue().getSortIndex()).isEqualTo(1);
        assertThat(bundleSubFolderDTO.getValue().getDocuments()).isNull();
    }

    private void assertDocumentImage(ListValue<Bundle> bundleDTO) {
        assertThat(bundleDTO.getValue().getDocumentImage().getCoordinateX().intValue()).isEqualTo(10);
        assertThat(bundleDTO.getValue().getDocumentImage().getCoordinateY().intValue()).isEqualTo(20);
        assertThat(bundleDTO.getValue().getDocumentImage().getDocmosisAssetId()).isEqualTo("12345");
        assertThat(bundleDTO.getValue().getDocumentImage().getImageRendering()).isNull();
        assertThat(bundleDTO.getValue().getDocumentImage().getImageRenderingLocation()).isNull();
    }

    private void assertSubFolder2(ListValue<BundleSubFolder> bundleSubFolderDTO) {
        ListValue<BundleSubFolder2> bundleSubSubFolderDTO = bundleSubFolderDTO.getValue().getFolders().get(0);
        assertThat(bundleSubSubFolderDTO.getValue().getName()).isEqualTo("firstSubSubFolder");
        assertThat(bundleSubSubFolderDTO.getValue().getSortIndex()).isEqualTo(1);
        assertThat(bundleSubSubFolderDTO.getValue().getDocuments()).isNull();
    }
}
