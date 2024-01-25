package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadResource;

class InvalidResourceExceptionTest {
    @Test
    void testInvalidResourceExceptionThrownForNonExistentFile() throws RuntimeException {
        String createCaseDataFileNotExist = "CICCaseDataNotExist.json";

        Exception exception = assertThrows(InvalidResourceException.class, () -> {
            byte [] caseDataJson = loadResource(createCaseDataFileNotExist);
            assertNull(caseDataJson);
        });

        assertTrue(exception.getMessage().contains(TEST_RESOURCE_NOT_FOUND));


    }
}
