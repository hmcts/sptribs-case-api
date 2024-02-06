package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadResource;

class InvalidResourceExceptionTest {
    @Test
    void invalidResourceExceptionThrownForNonExistentFile() throws RuntimeException {
        final String createCaseDataFileNotExist = "CICCaseDataNotExist.json";

        final InvalidResourceException invalidResourceException =
            assertThrows(InvalidResourceException.class, () ->
                loadResource(createCaseDataFileNotExist));

        assertTrue(invalidResourceException.getMessage().contains(TEST_RESOURCE_NOT_FOUND));
        assertNotNull(invalidResourceException.getCause());
    }
}
