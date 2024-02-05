package uk.gov.hmcts.sptribs.exception;

import java.io.Serial;

public class CaseCreateOrUpdateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7442994120484411079L;

    public CaseCreateOrUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaseCreateOrUpdateException(String message) {
        super(message);
    }
}
