package uk.gov.hmcts.sptribs.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.controllers.mapper.CicaCaseMapper;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.controllers.model.DashboardResponse;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.document.service.DocumentDownloadStatusService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Mock
    private CicaCaseMapper cicaCaseMapper;

    @Mock
    private DocumentDownloadStatusService documentDownloadStatusService;

    @Mock
    private IdamService idamService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DocumentController documentController;

    @Test
    void shouldReturnOkWithDocumentsForCcdReference() {
        // Given
        final DocumentEntity latestBundleDocument = DocumentEntity.builder()
            .caseReferenceNumber(1L)
            .build();

        final DocumentEntity contactPartyDocumentEntity = DocumentEntity.builder()
            .caseReferenceNumber(2L)
            .build();

        final List<ContactPartyDocumentDetails> contactPartyDocuments = List.of(
            new ContactPartyDocumentDetails(
                contactPartyDocumentEntity,
                OffsetDateTime.parse("2026-06-05T10:15:30Z")
            )
        );

        final List<DocumentEntity> orderAndDecisionDocuments = List.of(
            DocumentEntity.builder()
                .caseReferenceNumber(3L)
                .build()
        );

        final CaseworkerCICDocument mappedContactPartyDocument =
            CaseworkerCICDocument.builder().build();

        final CaseworkerCICDocument mappedOrderAndDecisionDocument =
            CaseworkerCICDocument.builder().build();

        final CaseworkerCICDocument mappedBundleDocument =
            CaseworkerCICDocument.builder().build();

        final DocumentDashboardModel dashboardModel = DocumentDashboardModel.builder()
            .contactPartiesDocuments(contactPartyDocuments)
            .orderAndDecisionDocuments(orderAndDecisionDocuments)
            .latestCaseBundleDocument(latestBundleDocument)
            .build();

        final CICUser user = mock(CICUser.class);
        final UserInfo userInfo = UserInfo.builder().sub("test-email@hmcts.net").build();
        when(user.getUserInfo()).thenReturn(userInfo);
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);

        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setEmail("test-email@hmcts.net");
        caseData.setCicCase(cicCase);

        final Map<String, JsonNode> dataMap = objectMapper.convertValue(
            caseData,
            new TypeReference<>() {}
        );

        final CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(dataMap)
            .build();

        final CicaCaseResponse cicaCaseResponse = CicaCaseResponse.builder().build();

        when(cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE))
            .thenReturn(cicaCaseEntity);

        when(cicaCaseMapper.toResponse(cicaCaseEntity))
            .thenReturn(cicaCaseResponse);

        when(documentsService.getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING)))
            .thenReturn(dashboardModel);

        when(caseworkerCICDocumentMapper.mapContactPartyDocument(contactPartyDocuments.get(0)))
            .thenReturn(mappedContactPartyDocument);

        when(caseworkerCICDocumentMapper.mapDocument(orderAndDecisionDocuments.get(0)))
            .thenReturn(mappedOrderAndDecisionDocument);

        when(caseworkerCICDocumentMapper.mapDocument(latestBundleDocument))
            .thenReturn(mappedBundleDocument);

        when(documentDownloadStatusService.getDownloadedDocumentIds(TEST_CASE_ID_STRING, Party.SUBJECT))
            .thenReturn(Set.of());

        // When
        ResponseEntity<DashboardResponse> response =
            documentController.getDocumentsByCCDReference(
                TEST_AUTHORIZATION,
                TEST_POSTCODE,
                TEST_CASE_ID_STRING
            );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getCicaCaseResponse()).isEqualTo(cicaCaseResponse);

        assertThat(response.getBody().getDocumentResponse().getContactPartiesDocuments().get(0).getDocument())
            .isEqualTo(mappedContactPartyDocument);

        assertThat(response.getBody().getDocumentResponse().getOrderAndDecisionDocuments().get(0).getDocument())
            .isEqualTo(mappedOrderAndDecisionDocument);

        assertThat(response.getBody().getDocumentResponse().getLatestCaseBundleDocuments().get(0).getDocument())
            .isEqualTo(mappedBundleDocument);

        verify(cicaCaseService).checkIfUserHasAccessWithPostcode(
            TEST_CASE_ID_STRING,
            TEST_AUTHORIZATION,
            TEST_POSTCODE
        );

        verify(cicaCaseMapper).toResponse(cicaCaseEntity);

        verify(documentsService)
            .getDocumentsOnCase(Long.valueOf(TEST_CASE_ID_STRING));
    }


    @Test
    void shouldReturnDownloadedDocument() {
        // Given
        final String documentId = "12345";
        final Resource resource = new ByteArrayResource("test-content".getBytes());

        final DownloadedDocumentResponse downloadedDocumentResponse =
            new DownloadedDocumentResponse(
                resource,
                "test-document.pdf",
                "application/pdf"
            );

        final CICUser user = mock(CICUser.class);
        final UserInfo userInfo = UserInfo.builder().sub("test-email@hmcts.net").build();
        when(user.getUserInfo()).thenReturn(userInfo);
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);

        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setEmail("test-email@hmcts.net");
        caseData.setCicCase(cicCase);

        final Map<String, JsonNode> dataMap = objectMapper.convertValue(
            caseData,
            new TypeReference<>() {}
        );

        final CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(dataMap)
            .build();

        when(cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE))
            .thenReturn(cicaCaseEntity);

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
    }

    @Test
    void shouldThrowExceptionWhenPostcodeValidationFailsOnDocumentDownload() {
        // Given
        String postcode = "INVALID";
        String documentId = "12345";

        when(cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, postcode))
            .thenThrow(new UnauthorisedCaseAccessException("Postcode or email mismatch"));

        // When / Then
        assertThatThrownBy(() -> documentController.downloadDocumentByCaseAndId(
            TEST_AUTHORIZATION, postcode, TEST_CASE_ID_STRING, documentId))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Postcode or email mismatch");

        verifyNoInteractions(documentDownloadService);
    }

    @Test
    void shouldThrowExceptionWhenCaseDataMissingOnGetDocuments() {
        // Given
        final CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(null)
            .build();

        when(cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE))
            .thenReturn(cicaCaseEntity);

        // When / Then
        assertThatThrownBy(() -> documentController.getDocumentsByCCDReference(
            TEST_AUTHORIZATION,
            TEST_POSTCODE,
            TEST_CASE_ID_STRING
        ))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Case data is missing");

        verifyNoInteractions(cicaCaseMapper, documentsService, documentDownloadStatusService);
    }

    @Test
    void shouldThrowExceptionWhenUserInfoMissingOnGetDocuments() {
        // Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setEmail("test-email@hmcts.net");
        caseData.setCicCase(cicCase);

        final Map<String, JsonNode> dataMap = objectMapper.convertValue(
            caseData,
            new TypeReference<>() {}
        );

        final CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(dataMap)
            .build();

        final CicaCaseResponse cicaCaseResponse = CicaCaseResponse.builder().build();

        final CICUser user = mock(CICUser.class);
        when(user.getUserInfo()).thenReturn(null);

        when(cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE))
            .thenReturn(cicaCaseEntity);
        when(cicaCaseMapper.toResponse(cicaCaseEntity)).thenReturn(cicaCaseResponse);
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);

        // When / Then
        assertThatThrownBy(() -> documentController.getDocumentsByCCDReference(
            TEST_AUTHORIZATION,
            TEST_POSTCODE,
            TEST_CASE_ID_STRING
        ))
            .isExactlyInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessageContaining("Unable to determine user identity");

        verifyNoInteractions(documentsService, documentDownloadStatusService);
    }
}
