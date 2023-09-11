package uk.gov.hmcts.sptribs.exception;

public class CaseCreateOrUpdateException extends RuntimeException {
    private static final long serialVersionUID = 7442994120484411079L;

    public CaseCreateOrUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaseCreateOrUpdateException(String message) {
        super(message);
    }
}
