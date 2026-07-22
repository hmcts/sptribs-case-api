package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.controllers.model.DocumentResponse;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.document.service.DocumentDownloadStatusService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_POSTCODE;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Mock
    private DocumentDownloadService documentDownloadService;

    @Mock
    private DocumentsService documentsService;

    @Mock
    private DocumentDownloadStatusService documentDownloadStatusService;

    @Mock
    private CaseworkerCICDocumentMapper caseworkerCICDocumentMapper;

    @Mock
    private CicaCaseService cicaCaseService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    void shouldReturnOkWithDocumentsForCcdReference() {
        // Given
        DocumentEntity latestBundleDocument = DocumentEntity.builder()
            .id(1L)
            .caseReferenceNumber(1L)
            .documentUrl("http://test-url-bundle")
            .build();

        List<DocumentEntity> contactPartyDocuments = List.of(
            DocumentEntity.builder()
                .id(2L)
                .caseReferenceNumber(2L)
                .documentUrl("http://test-url-contact")
                .build()
        );

        List<DocumentEntity> orderAndDecisionDocuments = List.of(
            DocumentEntity.builder()
                .id(3L)
                .caseReferenceNumber(3L)
                .documentUrl("http://test-url-order")
                .build()
        );

        CaseworkerCICDocument mappedContactPartyDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().url("http://test-url-contact").build())
            .build();
        CaseworkerCICDocument mappedOrderAndDecisionDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().url("http://test-url-order").build())
            .build();
        CaseworkerCICDocument mappedBundleDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().url("http://test-url-bundle").build())
            .build();

        DocumentDashboardModel dashboardModel = DocumentDashboardModel.builder()
            .contactPartiesDocuments(contactPartyDocuments)
            .orderAndDecisionDocuments(orderAndDecisionDocuments)
            .latestCaseBundleDocument(latestBundleDocument)
            .build();

        Set<Long> downloadedDocIds = Set.of(2L); // Contact document is downloaded

        when(cicaCaseService.verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE))
            .thenReturn(Party.SUBJECT);

        when(documentDownloadStatusService.getDownloadedDocumentIds(TEST_CASE_ID_STRING, Party.SUBJECT))
            .thenReturn(downloadedDocIds);

        when(documentsService.getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING)))
            .thenReturn(dashboardModel);

        when(caseworkerCICDocumentMapper.map(contactPartyDocuments))
            .thenReturn(List.of(mappedContactPartyDocument));

        when(caseworkerCICDocumentMapper.map(orderAndDecisionDocuments))
            .thenReturn(List.of(mappedOrderAndDecisionDocument));

        when(caseworkerCICDocumentMapper.mapEntityToList(latestBundleDocument))
            .thenReturn(List.of(mappedBundleDocument));

        // When
        ResponseEntity<DocumentResponse> response = documentController.getDocumentsByCCDReference(
            TEST_AUTHORIZATION,
            TEST_POSTCODE,
            TEST_CASE_ID_STRING
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getContactPartiesDocuments().getFirst().getDocument())
            .isEqualTo(mappedContactPartyDocument);
        assertThat(response.getBody().getContactPartiesDocuments().getFirst().isDownloaded())
            .isTrue();

        assertThat(response.getBody().getOrderAndDecisionDocuments().getFirst().getDocument())
            .isEqualTo(mappedOrderAndDecisionDocument);
        assertThat(response.getBody().getOrderAndDecisionDocuments().getFirst().isDownloaded())
            .isFalse();

        assertThat(response.getBody().getLatestCaseBundleDocuments().getFirst().getDocument())
            .isEqualTo(mappedBundleDocument);
        assertThat(response.getBody().getLatestCaseBundleDocuments().getFirst().isDownloaded())
            .isFalse();

        verify(cicaCaseService).verifyUserAccessAndGetParty(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION,
            TEST_POSTCODE
        );
        verify(documentDownloadStatusService).getDownloadedDocumentIds(TEST_CASE_ID_STRING, Party.SUBJECT);
        verify(documentsService).getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING));
        verify(caseworkerCICDocumentMapper).map(contactPartyDocuments);
        verify(caseworkerCICDocumentMapper).map(orderAndDecisionDocuments);
        verify(caseworkerCICDocumentMapper).mapEntityToList(latestBundleDocument);
    }


    @Test
    void shouldReturnDownloadedDocument() {
        // Given
        String documentId = "12345";
        Resource resource = new ByteArrayResource("test-content".getBytes());

        DownloadedDocumentResponse downloadedDocumentResponse =
            new DownloadedDocumentResponse(
                resource,
                "test-document.pdf",
                "application/pdf"
            );

        when(documentDownloadService.downloadDocument(
            TEST_AUTHORIZATION,
            documentId
        )).thenReturn(downloadedDocumentResponse);

        // When
        ResponseEntity<Resource> response = documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION,
            TEST_POSTCODE,
            TEST_CASE_ID_STRING,
            documentId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(resource);
        assertThat(response.getHeaders().getContentType().toString())
            .isEqualTo("application/pdf");
        assertThat(response.getHeaders().getFirst("original-file-name"))
            .isEqualTo("test-document.pdf");

        verify(cicaCaseService).verifyUserAccessAndGetParty(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION,
            TEST_POSTCODE
        );
        verify(documentDownloadService).downloadDocument(
            TEST_AUTHORIZATION,
            documentId
        );
        verify(documentDownloadStatusService).recordDocumentDownload(
            TEST_AUTHORIZATION,
            TEST_CASE_ID_STRING,
            TEST_POSTCODE,
            documentId
        );
    }

    @Test
    void shouldReturnDownloadedDocumentEvenIfRecordDownloadStatusFails() {
        // Given
        String documentId = "12345";
        Resource resource = new ByteArrayResource("test-content".getBytes());

        DownloadedDocumentResponse downloadedDocumentResponse =
            new DownloadedDocumentResponse(
                resource,
                "test-document.pdf",
                "application/pdf"
            );

        when(documentDownloadService.downloadDocument(
            TEST_AUTHORIZATION,
            documentId
        )).thenReturn(downloadedDocumentResponse);

        org.mockito.Mockito.doThrow(new RuntimeException("DB error"))
            .when(documentDownloadStatusService).recordDocumentDownload(
                TEST_AUTHORIZATION,
                TEST_CASE_ID_STRING,
                TEST_POSTCODE,
                documentId
            );

        // When
        ResponseEntity<Resource> response = documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION,
            TEST_POSTCODE,
            TEST_CASE_ID_STRING,
            documentId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(resource);
        verify(documentDownloadStatusService).recordDocumentDownload(
            TEST_AUTHORIZATION,
            TEST_CASE_ID_STRING,
            TEST_POSTCODE,
            documentId
        );
    }

    @Test
    void shouldThrowExceptionWhenPostcodeValidationFailsOnDocumentDownload() {
        // Given
        String postcode = "INVALID";
        String documentId = "12345";

        org.mockito.Mockito.doThrow(new UnauthorisedCaseAccessException("Postcode or email mismatch"))
            .when(cicaCaseService).verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, postcode);

        // When / Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION, postcode, TEST_CASE_ID_STRING, documentId))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Postcode or email mismatch");

        org.mockito.Mockito.verifyNoInteractions(documentDownloadService);
    }

    @Test
    void shouldReturnCorrectRoleSpecificEmbeddedDownloadFlagsForAuthenticatedParty() {
        // Given
        DocumentEntity contactDocument = DocumentEntity.builder()
            .id(201L)
            .caseReferenceNumber(2L)
            .documentUrl("http://url")
            .build();

        DocumentDashboardModel dashboardModel = DocumentDashboardModel.builder()
            .contactPartiesDocuments(List.of(contactDocument))
            .build();

        Set<Long> downloadedDocIds = Set.of(201L);

        CaseworkerCICDocument mappedDoc = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().url("http://url").build())
            .build();

        when(cicaCaseService.verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE))
            .thenReturn(Party.SUBJECT);

        when(documentDownloadStatusService.getDownloadedDocumentIds(TEST_CASE_ID_STRING, Party.SUBJECT))
            .thenReturn(downloadedDocIds);

        when(documentsService.getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING)))
            .thenReturn(dashboardModel);

        when(caseworkerCICDocumentMapper.map(List.of(contactDocument)))
            .thenReturn(List.of(mappedDoc));

        // When
        ResponseEntity<DocumentResponse> response = documentController.getDocumentsByCCDReference(
            TEST_AUTHORIZATION,
            TEST_POSTCODE,
            TEST_CASE_ID_STRING
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContactPartiesDocuments()).hasSize(1);
        assertThat(response.getBody().getContactPartiesDocuments().getFirst().isDownloaded()).isTrue();

        verify(documentDownloadStatusService).getDownloadedDocumentIds(TEST_CASE_ID_STRING, Party.SUBJECT);
        verify(caseworkerCICDocumentMapper).map(List.of(contactDocument));
    }
}
