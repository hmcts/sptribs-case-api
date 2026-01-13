package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.exception.DocumentDownloadException;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadServiceTest {

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private DocumentDownloadService documentDownloadService;

    @Test
    void shouldDownloadDocumentSuccessfully() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();
        byte[] documentContent = "test document content".getBytes();
        String fileName = "test-document.pdf";
        String mimeType = "application/pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = mimeType;

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(documentContent));

        // When
        DownloadedDocumentResponse response = documentDownloadService.downloadDocument(
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
    }

    @Test
    void shouldDownloadDocumentWithMimeTypeFromFileName() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();
        byte[] documentContent = "test document content".getBytes();
        String fileName = "test-document.pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = null;

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(documentContent));

        // When
        DownloadedDocumentResponse response = documentDownloadService.downloadDocument(
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
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();
        byte[] documentContent = "test document content".getBytes();
        String fileName = "test-document.unknown";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = null;

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(documentContent));

        // When
        DownloadedDocumentResponse response = documentDownloadService.downloadDocument(
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
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to get document metadata");
    }

    @Test
    void shouldThrowExceptionWhenDocumentBinaryNotFound() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();
        String fileName = "test-document.pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = "application/pdf";

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(null));

        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
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
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to download document");
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
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to get document metadata");
    }

    @Test
    void shouldThrowExceptionWhenBinaryResponseIsNull() {
        // Given
        UUID documentId = UUID.randomUUID();
        String documentIdString = documentId.toString();
        String fileName = "test-document.pdf";

        Document document = new Document();
        document.originalDocumentName = fileName;
        document.mimeType = "application/pdf";

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientApi.getDocument(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(ResponseEntity.ok(document));
        when(caseDocumentClientApi.getDocumentBinary(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, documentId))
            .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> documentDownloadService.downloadDocument(
            TEST_AUTHORIZATION_TOKEN,
            documentIdString
        ))
            .isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("Failed to download document binary");
    }
}


