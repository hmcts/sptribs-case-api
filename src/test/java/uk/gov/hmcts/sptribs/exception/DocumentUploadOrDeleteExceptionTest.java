package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.controllers.DocumentManagementController;
import uk.gov.hmcts.sptribs.model.DocumentInfo;
import uk.gov.hmcts.sptribs.services.DocumentManagementService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_UPLOAD_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_FILE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
class DocumentUploadOrDeleteExceptionTest {

    private DocumentInfo documentInfo;

    @InjectMocks
    private DocumentManagementController documentManagementController;

    @Mock
    DocumentManagementService documentManagementService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteDocumentUploadOrDeleteException() {
        documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        final String documentInfoId = documentInfo.getDocumentId();

        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            documentInfo.getDocumentId()))
            .thenThrow(
                new DocumentUploadOrDeleteException(
                    DOCUMENT_DELETE_FAILURE_MSG,
                    new RuntimeException()
                ));

        final DocumentUploadOrDeleteException documentUploadOrDeleteException =
            assertThrows(DocumentUploadOrDeleteException.class, () ->
                documentManagementController.deleteDocument(CASE_TEST_AUTHORIZATION,documentInfoId));

        assertTrue(documentUploadOrDeleteException.getMessage().contains(DOCUMENT_DELETE_FAILURE_MSG));
        assertNotNull(documentUploadOrDeleteException.getCause());
    }

    @Test
    void updateDocumentUploadOrDeleteException() throws IOException {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        final String documentInfoId = documentInfo.getDocumentId();

        MockMultipartFile multipartFile = new MockMultipartFile(
            JSON_FILE_TYPE,
            CASE_DATA_FILE_CIC,
            JSON_CONTENT_TYPE,
            caseDataJson.getBytes()
        );

        when(documentManagementService.uploadDocument(
            CASE_TEST_AUTHORIZATION,
            documentInfo.getDocumentId(),
            multipartFile))
            .thenThrow(
                new DocumentUploadOrDeleteException(
                DOCUMENT_UPLOAD_FAILURE_MSG,
                    new RuntimeException()
            ));

        final DocumentUploadOrDeleteException documentUploadOrDeleteException =
            assertThrows(DocumentUploadOrDeleteException.class, () ->
                documentManagementController.uploadDocument(CASE_TEST_AUTHORIZATION, documentInfoId,multipartFile));

        assertTrue(documentUploadOrDeleteException.getMessage().contains(DOCUMENT_UPLOAD_FAILURE_MSG));
        assertNotNull(documentUploadOrDeleteException.getCause());
    }
}
