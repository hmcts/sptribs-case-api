package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
class DocumentControllerIT {

    private static final String CCD_REFERENCE = "1234567891234567";
    private static final String POSTCODE = "SW11 1PD";
    private static final String DOWNLOAD_DOCUMENT_URL = "/cases/CIC/" + CCD_REFERENCE + "/documents/%s/download";
    private static final String GET_DOCUMENTS_URL = "/cases/CIC/" + CCD_REFERENCE + "/documents";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @MockitoBean
    private CicaCaseService cicaCaseService;

    @MockitoBean
    private DocumentsService documentsService;

    @MockitoBean
    private CaseworkerCICDocumentMapper caseworkerCICDocumentMapper;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldDownloadDocumentSuccessfully() throws Exception {
        // Given
        String fileName = "test-document.pdf";
        String mimeType = "application/pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = mimeType;
        UUID documentId = UUID.randomUUID();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(document));

        byte[] documentContent = "test document content".getBytes();
        when(caseDocumentClientApi.getDocumentBinary(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(documentContent));

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, documentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, mimeType))
            .andExpect(header().string("original-file-name", fileName))
            .andExpect(content().bytes(documentContent));
    }

    @Test
    void shouldReturn500WhenDocumentNotFound() throws Exception {
        // Given
        UUID documentId = UUID.randomUUID();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(null));

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, documentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturn500WhenDocumentBinaryNotFound() throws Exception {
        // Given
        String fileName = "test-document.pdf";
        String mimeType = "application/pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = mimeType;
        UUID documentId = UUID.randomUUID();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(null));

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, documentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturn500WhenApiCallFails() throws Exception {
        // Given
        UUID documentId = UUID.randomUUID();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenThrow(new RuntimeException("API error"));

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, documentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturn500ForInvalidDocumentId() throws Exception {
        // Given
        String invalidDocumentId = "invalid-uuid-format";

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, invalidDocumentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldDownloadDocumentWithDifferentMimeTypes() throws Exception {
        // Given
        String fileName = "test-document.html";
        String mimeType = "text/html";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = mimeType;

        UUID documentId = UUID.randomUUID();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(document));

        byte[] documentContent = "<html><body>Test</body></html>".getBytes();
        when(caseDocumentClientApi.getDocumentBinary(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(documentId)
        )).thenReturn(ResponseEntity.ok(documentContent));

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, documentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, mimeType))
            .andExpect(header().string("original-file-name", fileName))
            .andExpect(content().bytes(documentContent));
    }

    @Test
    void shouldGetDocumentsSuccessfully() throws Exception {
        // Given
        DocumentDashboardModel dashboardModel = DocumentDashboardModel.builder()
            .contactPartiesDocuments(emptyList())
            .orderAndDecisionDocuments(emptyList())
            .latestCaseBundleDocument(null)
            .build();

        when(documentsService.getDocumentsOnCase(Long.valueOf(CCD_REFERENCE)))
            .thenReturn(dashboardModel);

        when(caseworkerCICDocumentMapper.map(anyList())).thenReturn(emptyList());
        when(caseworkerCICDocumentMapper.mapEntityToList(any())).thenReturn(emptyList());

        // When & Then
        mockMvc.perform(get(GET_DOCUMENTS_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"contactPartiesDocuments\":[],\"orderAndDecisionDocuments\":[],\"latestCaseBundleDocuments\":[]}"));
    }

    @Test
    void shouldFailToGetDocumentsWhenPostcodeValidationFails() throws Exception {
        // Given
        String invalidPostcode = "INVALID";
        org.mockito.Mockito.doThrow(new UnauthorisedCaseAccessException("Postcode match failed"))
            .when(cicaCaseService).checkIfUserHasAccessWithPostcode(eq(CCD_REFERENCE), any(), eq(invalidPostcode));

        // When & Then
        mockMvc.perform(get(GET_DOCUMENTS_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", invalidPostcode))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailToDownloadDocumentWhenPostcodeValidationFails() throws Exception {
        // Given
        UUID documentId = UUID.randomUUID();
        String invalidPostcode = "INVALID";
        org.mockito.Mockito.doThrow(new UnauthorisedCaseAccessException("Postcode match failed"))
            .when(cicaCaseService).checkIfUserHasAccessWithPostcode(eq(CCD_REFERENCE), any(), eq(invalidPostcode));

        // When & Then
        mockMvc.perform(get(String.format(DOWNLOAD_DOCUMENT_URL, documentId))
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", invalidPostcode))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400WhenGetDocumentsWithInvalidCcdReference() throws Exception {
        // Given
        String invalidCcdReference = "1234";
        String url = "/cases/CIC/" + invalidCcdReference + "/documents";

        // When & Then
        mockMvc.perform(get(url)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDownloadDocumentWithInvalidCcdReference() throws Exception {
        // Given
        String invalidCcdReference = "1234";
        UUID documentId = UUID.randomUUID();
        String url = "/cases/CIC/" + invalidCcdReference + "/documents/" + documentId + "/download";

        // When & Then
        mockMvc.perform(get(url)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header("X-Postcode", POSTCODE))
            .andExpect(status().isBadRequest());
    }
}







