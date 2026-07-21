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
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.controllers.model.DocumentResponse;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;

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
    private CaseworkerCICDocumentMapper caseworkerCICDocumentMapper;

    @Mock
    private CicaCaseService cicaCaseService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    void shouldReturnOkWithDocumentsForCcdReference() {
        // Given
        DocumentEntity latestBundleDocument = DocumentEntity.builder()
            .caseReferenceNumber(1L)
            .build();

        List<DocumentEntity> contactPartyDocuments = List.of(
            DocumentEntity.builder()
                .caseReferenceNumber(2L)
                .build()
        );

        List<DocumentEntity> orderAndDecisionDocuments = List.of(
            DocumentEntity.builder()
                .caseReferenceNumber(3L)
                .build()
        );

        CaseworkerCICDocument mappedContactPartyDocument = CaseworkerCICDocument.builder().build();
        CaseworkerCICDocument mappedOrderAndDecisionDocument = CaseworkerCICDocument.builder().build();
        CaseworkerCICDocument mappedBundleDocument = CaseworkerCICDocument.builder().build();

        DocumentDashboardModel dashboardModel = DocumentDashboardModel.builder()
            .contactPartiesDocuments(contactPartyDocuments)
            .orderAndDecisionDocuments(orderAndDecisionDocuments)
            .latestCaseBundleDocument(latestBundleDocument)
            .build();

        Set<Long> downloadedDocIds = Set.of(101L);

        when(documentsService.getDownloadedDocumentIds(TEST_AUTHORIZATION, TEST_CASE_ID_STRING, TEST_POSTCODE))
            .thenReturn(downloadedDocIds);

        when(documentsService.getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING)))
            .thenReturn(dashboardModel);

        when(caseworkerCICDocumentMapper.map(contactPartyDocuments, downloadedDocIds))
            .thenReturn(List.of(mappedContactPartyDocument));

        when(caseworkerCICDocumentMapper.map(orderAndDecisionDocuments, downloadedDocIds))
            .thenReturn(List.of(mappedOrderAndDecisionDocument));

        when(caseworkerCICDocumentMapper.mapEntityToList(latestBundleDocument, downloadedDocIds))
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

        assertThat(response.getBody().getContactPartiesDocuments())
            .containsExactly(mappedContactPartyDocument);

        assertThat(response.getBody().getOrderAndDecisionDocuments())
            .containsExactly(mappedOrderAndDecisionDocument);

        assertThat(response.getBody().getLatestCaseBundleDocuments())
            .containsExactly(mappedBundleDocument);

        verify(cicaCaseService).checkIfUserHasAccessWithPostcode(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION,
            TEST_POSTCODE
        );
        verify(documentsService).getDownloadedDocumentIds(TEST_AUTHORIZATION, TEST_CASE_ID_STRING, TEST_POSTCODE);
        verify(documentsService).getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING));
        verify(caseworkerCICDocumentMapper).map(contactPartyDocuments, downloadedDocIds);
        verify(caseworkerCICDocumentMapper).map(orderAndDecisionDocuments, downloadedDocIds);
        verify(caseworkerCICDocumentMapper).mapEntityToList(latestBundleDocument, downloadedDocIds);
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

        verify(cicaCaseService).checkIfUserHasAccessWithPostcode(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION,
            TEST_POSTCODE
        );
        verify(documentDownloadService).downloadDocument(
            TEST_AUTHORIZATION,
            documentId
        );
        verify(documentsService).recordDocumentDownload(
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
            .when(documentsService).recordDocumentDownload(
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
        verify(documentsService).recordDocumentDownload(
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
            .when(cicaCaseService).checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, postcode);

        // When / Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION, postcode, TEST_CASE_ID_STRING, documentId))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Postcode or email mismatch");

        org.mockito.Mockito.verifyNoInteractions(documentDownloadService);
    }
}
