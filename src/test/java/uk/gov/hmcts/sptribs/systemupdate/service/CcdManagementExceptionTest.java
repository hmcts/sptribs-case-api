package uk.gov.hmcts.sptribs.systemupdate.service;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class CcdManagementExceptionTest {

    @Test
    void shouldThrowCcdManagementException() {

        final String message = "Failed to Search Cases";

        final CcdManagementException ccdManagementException = new CcdManagementException(message, new Throwable());

        assertEquals(message, ccdManagementException.getMessage());
        assertNotNull(ccdManagementException.getCause());

    }

    @Test
    void shouldThrowCcdManagementExceptionWithStatus() {

        final String message = "Failed to Search Cases";
        final int status = NOT_FOUND.value();

        final CcdManagementException ccdManagementException = new CcdManagementException(status,message, new Throwable());

        assertEquals(message, ccdManagementException.getMessage());
        assertEquals(status, ccdManagementException.getStatus());
        assertNotNull(ccdManagementException.getCause());

    }
}
