package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.sptribs.cdam.model.Classification;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DocumentManagementControllerIT {

    private static final String DOCUMENT_UPLOAD_RESPONSE = "classpath:citizen-document-upload-response.json";

    private static final String AUTH_TOKEN = "test-auth";
    private static final String S2S_TOKEN = "s2s-token";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String CASE_TYPE = "CIC";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSuccessfullyUploadDocumentWhenUploadControllerTriggered() throws Exception {

        final MockMultipartFile file = new MockMultipartFile(
            "file",
            "orig.png",
            MediaType.IMAGE_PNG_VALUE,
            "bar".getBytes()
        );

        final UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setDocuments(populateDocuments(file));

        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(caseDocumentClientApi.uploadDocuments(
            eq(AUTH_TOKEN),
            eq(S2S_TOKEN),
            any(DocumentUploadRequest.class)
        )).thenReturn(uploadResponse);

        final String response = mockMvc.perform(multipart("/doc/dss-orchestration/upload")
            .file(file)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .param(CASE_TYPE_OF_APPLICATION, CASE_TYPE)
            .contentType(MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(DOCUMENT_UPLOAD_RESPONSE)));
    }

    @Test
    void shouldSuccessfullyDeleteDocumentWhenDeleteDocumentControllerTriggered() throws Exception {
        String documentId = "73e8e59d-61aa-453f-abbd-3d1c6ff2557c";
        UUID documentUUID = UUID.fromString(documentId);

        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        doNothing()
            .when(caseDocumentClientApi)
            .deleteDocument(
                AUTH_TOKEN,
                S2S_TOKEN,
                documentUUID,
                true
            );

        mockMvc.perform(delete("/doc/dss-orchestration/" + documentId + "/delete")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Success"));
    }

    private List<Document> populateDocuments(MockMultipartFile file) {
        final Document.Links links = new Document.Links();
        links.binary = new Document.DocumentLink();
        links.self = new Document.DocumentLink();
        links.binary.href = "binaryUrl";
        links.self.href = "selfURL";

        final Document document = new Document();
        document.setClassification(Classification.RESTRICTED);
        document.setOriginalDocumentName(file.getOriginalFilename());
        document.setHashToken("SomeToken");
        document.setSize(file.getSize());
        document.setMimeType(JSON_CONTENT_TYPE);
        document.links = links;

        final List<Document> documents = new ArrayList<>();
        documents.add(document);

        return documents;
    }
}
