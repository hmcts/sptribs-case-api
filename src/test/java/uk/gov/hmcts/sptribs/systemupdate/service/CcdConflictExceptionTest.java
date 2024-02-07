package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CcdConflictExceptionTest {

    @Test
    void shouldThrowCcdConflictException() {
        final String message = "Failed Search because of a conflict";

        final CcdConflictException ccdConflictException = new CcdConflictException(message,new Throwable());

        assertEquals(message,ccdConflictException.getMessage());
        assertNotNull(ccdConflictException.getCause());
    }
}
