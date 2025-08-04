package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.sptribs.exception.DocumentUploadOrDeleteException;
import uk.gov.hmcts.sptribs.model.DocumentInfo;
import uk.gov.hmcts.sptribs.model.DocumentResponse;
import uk.gov.hmcts.sptribs.services.DocumentManagementService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_UPLOAD_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_FILE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.RESPONSE_STATUS_SUCCESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(MockitoExtension.class)
class DocumentManagementControllerTest {

    @InjectMocks
    private DocumentManagementController documentManagementController;

    @Mock
    private DocumentManagementService documentManagementService;


    private final DocumentInfo documentInfo = DocumentInfo.builder()
        .documentId(CASE_DATA_CIC_ID)
        .url(TEST_URL)
        .fileName(CASE_DATA_FILE_CIC).build();
    private final DocumentResponse documentResponseSuccess = DocumentResponse.builder()
        .status(RESPONSE_STATUS_SUCCESS)
        .document(documentInfo).build();

    @Test
    void testCicDocumentControllerFileUpload() throws IOException {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

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
            documentResponseSuccess);

        final ResponseEntity<?> uploadDocumentResponse = documentManagementController.uploadDocument(
            CASE_TEST_AUTHORIZATION,
            CASE_DATA_CIC_ID,
            multipartFile
        );

        final DocumentResponse testResponse = (DocumentResponse) uploadDocumentResponse.getBody();

        verify(documentManagementService, times(1)).uploadDocument(any(), any(), any());
        assertEquals(HttpStatus.OK, uploadDocumentResponse.getStatusCode());
        assertNotNull(testResponse);
        assertEquals(documentInfo.getDocumentId(), testResponse.getDocument().getDocumentId());
        assertEquals(documentInfo.getFileName(), testResponse.getDocument().getFileName());
        assertEquals(documentInfo.getUrl(), testResponse.getDocument().getUrl());
        assertEquals(RESPONSE_STATUS_SUCCESS, testResponse.getStatus());
    }

    @Test
    void testUploadDocumentControllerFailsWithException() throws IOException {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

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
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_UPLOAD_FAILURE_MSG,
                new Throwable()
            ));

        final DocumentUploadOrDeleteException exception = assertThrows(DocumentUploadOrDeleteException.class,
            () -> documentManagementService.uploadDocument(
                CASE_TEST_AUTHORIZATION,
                CASE_DATA_CIC_ID,
                multipartFile
            ));

        verify(documentManagementService, times(1)).uploadDocument(any(), any(), any());
        assertTrue(exception.getMessage().contains(DOCUMENT_UPLOAD_FAILURE_MSG));
    }

    @Test
    void testDeleteCicDocumentController() {
        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            CASE_DATA_CIC_ID
        )).thenReturn(documentResponseSuccess);

        final ResponseEntity<?> response = documentManagementController.deleteDocument(CASE_TEST_AUTHORIZATION, CASE_DATA_CIC_ID);

        verify(documentManagementService, times(1)).deleteDocument(any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final DocumentResponse testResponse = (DocumentResponse) response.getBody();
        assertNotNull(testResponse);
        assertEquals(documentInfo.getDocumentId(), testResponse.getDocument().getDocumentId());
        assertEquals(documentInfo.getFileName(), testResponse.getDocument().getFileName());
        assertEquals(documentInfo.getUrl(), testResponse.getDocument().getUrl());
        assertEquals(RESPONSE_STATUS_SUCCESS, testResponse.getStatus());
    }

    @Test
    void testDeleteCicDocumentControllerFailedWithException() {
        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            CASE_DATA_CIC_ID
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_DELETE_FAILURE_MSG,
                new Throwable()
            ));

        final DocumentUploadOrDeleteException exception = assertThrows(DocumentUploadOrDeleteException.class,
            () -> documentManagementService.deleteDocument(CASE_TEST_AUTHORIZATION, CASE_DATA_CIC_ID));
        verify(documentManagementService, times(1)).deleteDocument(any(), any());
        assertTrue(exception.getMessage().contains(DOCUMENT_DELETE_FAILURE_MSG));
    }
}
