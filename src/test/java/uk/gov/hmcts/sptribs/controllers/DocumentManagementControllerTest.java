package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.exception.DocumentUploadOrDeleteException;
import uk.gov.hmcts.sptribs.model.DocumentInfo;
import uk.gov.hmcts.sptribs.model.DocumentResponse;
import uk.gov.hmcts.sptribs.services.DocumentManagementService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_FILE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.RESPONSE_STATUS_SUCCESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class DocumentManagementControllerTest {

    @InjectMocks
    private DocumentManagementController documentManagementController;

    @Mock
    private DocumentManagementService documentManagementService;

    private AutoCloseable closeableMocks;

    @BeforeEach
    void setUp() {
        closeableMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeableMocks.close();
    }

    @Test
    void testCicDocumentControllerFileUpload() throws IOException {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final DocumentInfo document = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        final DocumentResponse documentResponse = DocumentResponse.builder()
            .status(RESPONSE_STATUS_SUCCESS)
            .document(document).build();

        final MockMultipartFile multipartFile = new MockMultipartFile(
            JSON_FILE_TYPE,
                CASE_DATA_FILE_CIC,
            JSON_CONTENT_TYPE,
            caseDataJson.getBytes()
        );

        when(documentManagementService.uploadDocument(
            CASE_TEST_AUTHORIZATION,
                CASE_DATA_CIC_ID,
            multipartFile
        )).thenReturn(
            documentResponse);

        final ResponseEntity<?> uploadDocumentResponse = documentManagementController.uploadDocument(
            CASE_TEST_AUTHORIZATION,
                CASE_DATA_CIC_ID,
            multipartFile
        );

        final DocumentResponse testResponse = (DocumentResponse) uploadDocumentResponse.getBody();

        assertEquals(HttpStatus.OK, uploadDocumentResponse.getStatusCode());
        assertNotNull(testResponse);
        assertEquals(document.getDocumentId(), testResponse.getDocument().getDocumentId());
        assertEquals(document.getFileName(), testResponse.getDocument().getFileName());
        assertEquals(document.getUrl(), testResponse.getDocument().getUrl());
        assertEquals(RESPONSE_STATUS_SUCCESS, testResponse.getStatus());
    }

    @Test
    void testDeleteCicDocumentControllerFailedWithException() {
        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            documentInfo.getDocumentId()
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_DELETE_FAILURE_MSG,
                new Throwable()
            ));

        assertThrows(DocumentUploadOrDeleteException.class,
            () -> documentManagementService.deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId()));
    }

    @Test
    void testDeleteDocumentControllerExceptionMessage() {
        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            documentInfo.getDocumentId()
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_DELETE_FAILURE_MSG,
                new Throwable()
            ));

        try {
            documentManagementService.deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId());
        } catch (DocumentUploadOrDeleteException documentUploadOrDeleteException) {
            assertTrue(documentUploadOrDeleteException.getMessage().contains(DOCUMENT_DELETE_FAILURE_MSG));
        }
    }
}
