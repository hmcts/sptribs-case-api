package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_UPLOAD_FAILURE_MSG;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class DocumentUploadOrDeleteExceptionTest {

    @Test
    void testDocumentUploadException() throws Exception {

        String message = DOCUMENT_UPLOAD_FAILURE_MSG;
        DocumentUploadOrDeleteException exception = new DocumentUploadOrDeleteException(DOCUMENT_UPLOAD_FAILURE_MSG, new Throwable());

        assertEquals(message, exception.getMessage());
        assertNotNull(exception.getCause());

    }

    @Test
    void testInvalidResourceExceptionWithMessageandClause() throws Exception {

        String message = DOCUMENT_DELETE_FAILURE_MSG;
        DocumentUploadOrDeleteException exception = new DocumentUploadOrDeleteException(DOCUMENT_DELETE_FAILURE_MSG, new Throwable());

        assertEquals(message, exception.getMessage());
        assertNotNull(exception.getCause());

    }
}

