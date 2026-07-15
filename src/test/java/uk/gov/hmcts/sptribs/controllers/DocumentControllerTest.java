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
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        String ccdReference = "1234567891234567";

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

        String postcode = "SW11 1PD";
        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder().id(ccdReference).build();

        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)).thenReturn(cicaCaseEntity);

        when(documentsService.getDocumentsOnCase(Long.valueOf(ccdReference)))
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
            postcode,
            ccdReference
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

        verify(cicaCaseService).getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION);
        verify(cicaCaseService).validatePostcode(cicaCaseEntity, postcode);
        verify(documentsService).getDocumentsOnCase(Long.valueOf(ccdReference));
        verify(caseworkerCICDocumentMapper).map(contactPartyDocuments);
        verify(caseworkerCICDocumentMapper).map(orderAndDecisionDocuments);
        verify(caseworkerCICDocumentMapper).mapEntityToList(latestBundleDocument);
    }

    @Test
    void shouldThrowExceptionWhenPostcodeValidationFailsOnDocumentRetrieval() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "INVALID";
        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder().id(ccdReference).build();

        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)).thenReturn(cicaCaseEntity);
        org.mockito.Mockito.doThrow(new UnauthorisedCaseAccessException("Postcode match failed"))
            .when(cicaCaseService).validatePostcode(cicaCaseEntity, postcode);

        // When / Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> documentController.getDocumentsByCCDReference(
            TEST_AUTHORIZATION,
            postcode,
            ccdReference
        ))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Postcode match failed");

        org.mockito.Mockito.verifyNoInteractions(documentsService);
    }

    @Test
    void shouldReturnDownloadedDocument() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";
        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder().id(ccdReference).build();
        String documentId = "12345";
        Resource resource = new ByteArrayResource("test-content".getBytes());

        DownloadedDocumentResponse downloadedDocumentResponse =
            new DownloadedDocumentResponse(
                resource,
                "test-document.pdf",
                "application/pdf"
            );

        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)).thenReturn(cicaCaseEntity);

        when(documentDownloadService.downloadDocument(
            TEST_AUTHORIZATION,
            documentId
        )).thenReturn(downloadedDocumentResponse);

        // When
        ResponseEntity<Resource> response = documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION,
            postcode,
            ccdReference,
            documentId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(resource);
        assertThat(response.getHeaders().getContentType().toString())
            .isEqualTo("application/pdf");
        assertThat(response.getHeaders().getFirst("original-file-name"))
            .isEqualTo("test-document.pdf");

        verify(cicaCaseService).getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION);
        verify(cicaCaseService).validatePostcode(cicaCaseEntity, postcode);
        verify(documentDownloadService).downloadDocument(
            TEST_AUTHORIZATION,
            documentId
        );
    }

    @Test
    void shouldThrowExceptionWhenPostcodeValidationFailsOnDocumentDownload() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "INVALID";
        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder().id(ccdReference).build();
        String documentId = "12345";

        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)).thenReturn(cicaCaseEntity);
        org.mockito.Mockito.doThrow(new UnauthorisedCaseAccessException("Postcode match failed"))
            .when(cicaCaseService).validatePostcode(cicaCaseEntity, postcode);

        // When / Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION,
            postcode,
            ccdReference,
            documentId
        ))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Postcode match failed");

        org.mockito.Mockito.verifyNoInteractions(documentDownloadService);
    }
}
