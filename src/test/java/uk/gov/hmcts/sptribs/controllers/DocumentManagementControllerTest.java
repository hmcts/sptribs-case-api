package uk.gov.hmcts.reform.cosapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cosapi.exception.DocumentUploadOrDeleteException;
import uk.gov.hmcts.reform.cosapi.model.DocumentInfo;
import uk.gov.hmcts.reform.cosapi.model.DocumentResponse;
import uk.gov.hmcts.reform.cosapi.services.DocumentManagementService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.JSON_FILE_TYPE;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.RESPONSE_STATUS_SUCCESS;
import static uk.gov.hmcts.reform.cosapi.util.TestConstant.TEST_URL;
import static uk.gov.hmcts.reform.cosapi.util.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class DocumentManagementControllerTest {

    @InjectMocks
    private DocumentManagementController documentManagementController;

    @Mock
    DocumentManagementService documentManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCicDocumentControllerFileUpload() throws Exception {
        String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        DocumentInfo document = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        DocumentResponse documentResponse = DocumentResponse.builder()
            .status(RESPONSE_STATUS_SUCCESS)
            .document(document).build();

        MockMultipartFile multipartFile = new MockMultipartFile(
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

        ResponseEntity<?> uploadDocumentResponse = documentManagementController.uploadDocument(
            CASE_TEST_AUTHORIZATION,
                CASE_DATA_CIC_ID,
            multipartFile
        );

        DocumentResponse testResponse = (DocumentResponse) uploadDocumentResponse.getBody();

        assertNotNull(testResponse);
        assertEquals(document.getDocumentId(), testResponse.getDocument().getDocumentId());
        assertEquals(document.getFileName(), testResponse.getDocument().getFileName());
        assertEquals(document.getUrl(), testResponse.getDocument().getUrl());
        assertEquals(RESPONSE_STATUS_SUCCESS, testResponse.getStatus());
    }

    @Test
    void testDeleteCicDocumentControllerFailedWithException() throws Exception {
        DocumentInfo documentInfo = DocumentInfo.builder()
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

        Exception exception = assertThrows(Exception.class, () -> {
            documentManagementService.deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId());
        });
        assertTrue(exception.getMessage().contains(DOCUMENT_DELETE_FAILURE_MSG));
    }
}
