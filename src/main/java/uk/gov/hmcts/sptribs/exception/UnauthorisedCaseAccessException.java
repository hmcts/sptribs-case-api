package uk.gov.hmcts.sptribs.exception;

import java.io.Serial;

public class UnauthorisedCaseAccessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorisedCaseAccessException(String message) {
        super(message);
    }
}
