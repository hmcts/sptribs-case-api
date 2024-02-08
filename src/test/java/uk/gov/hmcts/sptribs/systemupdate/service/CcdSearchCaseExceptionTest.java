package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CcdSearchCaseExceptionTest {

    @Test
    void shouldThrowCcdSearchCaseException() {

        final String message = "Failed to Search Cases";

        final CcdSearchCaseException ccdSearchCaseException = new CcdSearchCaseException(message, new Throwable());

        assertEquals(message, ccdSearchCaseException.getMessage());
        assertNotNull(ccdSearchCaseException.getCause());

    }
}
