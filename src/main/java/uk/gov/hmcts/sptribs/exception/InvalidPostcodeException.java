package uk.gov.hmcts.sptribs.exception;

import java.io.Serial;

public class InvalidPostcodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
