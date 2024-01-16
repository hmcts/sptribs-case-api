package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_RESOURCE_NOT_FOUND;



@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class InvalidResourceExceptionTest {


    @Test
    void testInvalidResourceExceptionWithMessage() throws Exception {

        String message = TEST_RESOURCE_NOT_FOUND;
        Exception cause = new Exception();
        InvalidResourceException exception = new InvalidResourceException(TEST_RESOURCE_NOT_FOUND);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());

    }

    @Test
    void testInvalidResourceExceptionWithMessageandClause() throws Exception {

        String message = TEST_RESOURCE_NOT_FOUND;
        Exception cause = new Exception();
        InvalidResourceException exception = new InvalidResourceException(TEST_RESOURCE_NOT_FOUND, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());

    }
}
