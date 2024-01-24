package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.controllers.DocumentManagementController;
import uk.gov.hmcts.sptribs.model.DocumentInfo;
import uk.gov.hmcts.sptribs.services.DocumentManagementService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_UPLOAD_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
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
    void testDeleteDocumentUploadOrDeleteException() throws Exception {
        documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        when(documentManagementService.deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId())).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_DELETE_FAILURE_MSG,
                new RuntimeException()
            ));

        Exception exception = assertThrows(Exception.class, () -> {
            documentManagementController.deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId());
        });
        assertTrue(exception.getMessage().contains(DOCUMENT_DELETE_FAILURE_MSG),
            String.valueOf(true));
    }

    @Test
    void testUpdateDocumentUploadOrDeleteException() throws Exception {
        documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            documentInfo.getDocumentId()
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_UPLOAD_FAILURE_MSG,
                new RuntimeException()
            ));

        Exception exception = assertThrows(Exception.class, () -> {
            documentManagementController.deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId());
        });
        assertTrue(
            exception.getMessage().contains(DOCUMENT_UPLOAD_FAILURE_MSG),
            String.valueOf(true)
        );
    }
}
