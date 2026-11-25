package uk.gov.hmcts.sptribs.document;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.service.CicaRoleService;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.DocumentDownloadException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadServiceTest {

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CicaRoleService cicaRoleService;

    @InjectMocks
    private DocumentDownloadService documentDownloadService;

    @Test
    void shouldDownloadDocumentSuccessfully() {
        // Given
        String fileName = "test-document.pdf";
        String mimeType = "application/pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = mimeType;

        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        byte[] documentContent = "test document content".getBytes();
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(documentContent));

        String documentIdString = documentId.toString();

        // When
        DownloadedDocumentResponse response = documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.fileName()).isEqualTo(fileName);
        assertThat(response.mimeType()).isEqualTo(mimeType);
        assertThat(response.file()).isInstanceOf(ByteArrayResource.class);

        verify(authTokenGenerator).generate();
        verify(caseDocumentClientApi).getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        verify(caseDocumentClientApi).getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        verifyNoMoreInteractions(authTokenGenerator, caseDocumentClientApi);
        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldDownloadDocumentWithMimeTypeFromFileName() {
        // Given
        String fileName = "test-document.pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = null;

        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        byte[] documentContent = "test document content".getBytes();
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(documentContent));

        String documentIdString = documentId.toString();
        // When
        DownloadedDocumentResponse response = documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.fileName()).isEqualTo(fileName);
        assertThat(response.mimeType()).isEqualTo("application/pdf");
    }

    @Test
    void shouldReturnOctetStreamForUnknownMimeType() {
        // Given
        String fileName = "test-document.unknown";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = null;

        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));

        byte[] documentContent = "test document content".getBytes();
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(documentContent));

        String documentIdString = documentId.toString();
        // When
        DownloadedDocumentResponse response = documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.mimeType()).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldThrowExceptionForInvalidDocumentIdFormat() {
        // Given
        String invalidDocumentId = "invalid-uuid-format";

        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            invalidDocumentId
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Invalid document ID format");
    }

    @Test
    void shouldThrowExceptionWhenDocumentMetadataNotFound() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(null));

        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to get document metadata");
    }

    @Test
    void shouldThrowExceptionWhenDocumentBinaryNotFound() {
        // Given
        String fileName = "test-document.pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = "application/pdf";

        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(null));

        String documentIdString = documentId.toString();
        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to download document binary");
    }

    @Test
    void shouldThrowExceptionWhenApiCallFails() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenThrow(new RuntimeException("API error"));

        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to download document");
    }

    @Test
    void shouldEnsureCreatorRoleAndRetryMetadataOnceWithCitizenToken() {
        UUID documentId = UUID.randomUUID();
        FeignException notFound = failure(404);
        Document document = new Document();
        document.originalDocumentName = "test.pdf";
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenThrow(notFound).thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok("document".getBytes()));

        var downloaded = documentDownloadService.downloadDocument(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN,
            documentId.toString());

        assertThat(downloaded.fileName()).isEqualTo("test.pdf");
        var calls = inOrder(caseDocumentClientApi, cicaRoleService);
        calls.verify(caseDocumentClientApi).getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        calls.verify(cicaRoleService).ensureCreatorRole(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN);
        calls.verify(caseDocumentClientApi).getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        calls.verify(caseDocumentClientApi).getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        calls.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotEnsureRoleOrRetryWhenBinaryIsMissingAfterMetadataSucceeds() {
        UUID documentId = UUID.randomUUID();
        FeignException notFound = failure(404);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(new Document()));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenThrow(notFound);

        assertThatThrownBy(() -> documentDownloadService.downloadDocument(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN,
            documentId.toString())).isInstanceOf(CaseNotFoundException.class);

        verifyNoInteractions(cicaRoleService);
        verify(caseDocumentClientApi).getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        verify(caseDocumentClientApi).getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 500, 503, -1})
    void shouldNotEnsureRoleForUnrelatedMetadataFailure(int status) {
        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenThrow(failure(status));

        assertThatThrownBy(() -> documentDownloadService.downloadDocument(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN,
            documentId.toString()))
            .isInstanceOf(status == 403 ? UnauthorisedCaseAccessException.class : DocumentDownloadException.class);

        verifyNoInteractions(cicaRoleService);
        verify(caseDocumentClientApi).getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        verify(caseDocumentClientApi, never()).getDocumentBinary(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 404, 503})
    void shouldPreserveRetryFailureAndStopAfterOneRecovery(int status) {
        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenThrow(failure(404), failure(status));

        Class<? extends RuntimeException> expected = switch (status) {
            case 403 -> UnauthorisedCaseAccessException.class;
            case 404 -> CaseNotFoundException.class;
            default -> DocumentDownloadException.class;
        };
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN,
            documentId.toString())).isInstanceOf(expected);

        verify(cicaRoleService).ensureCreatorRole(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN);
        verify(caseDocumentClientApi, times(2)).getDocument(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        verify(caseDocumentClientApi, never()).getDocumentBinary(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
    }

    @Test
    void shouldNotRetryDownloadWhenRoleLookupOrAssignmentFails() {
        UUID documentId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenThrow(failure(404));
        IllegalStateException lookupFailure = new IllegalStateException("Role lookup unavailable");
        doThrow(lookupFailure).when(cicaRoleService).ensureCreatorRole(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN);

        assertThatThrownBy(() -> documentDownloadService.downloadDocument(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN,
            documentId.toString())).isInstanceOf(DocumentDownloadException.class).hasCause(lookupFailure);

        verify(caseDocumentClientApi).getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
        verify(caseDocumentClientApi, never()).getDocumentBinary(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId);
    }

    private FeignException failure(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://cdam/documents",
            Map.of(), null, StandardCharsets.UTF_8);
        Response response = Response.builder().status(status).request(request).build();
        return FeignException.errorStatus("getDocument", response);
    }

    @Test
    void shouldThrowExceptionWhenMetadataResponseIsNull() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to get document metadata");
    }

    @Test
    void shouldThrowExceptionWhenBinaryResponseIsNull() {
        // Given
        String fileName = "test-document.pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = "application/pdf";
        UUID documentId = UUID.randomUUID();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(null);

        String documentIdString = documentId.toString();
        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to download document binary");
    }
}
