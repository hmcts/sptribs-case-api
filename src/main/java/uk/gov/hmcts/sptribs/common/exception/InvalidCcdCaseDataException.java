package uk.gov.hmcts.sptribs.common.exception;

public class InvalidCcdCaseDataException extends RuntimeException {

    private static final long serialVersionUID = 1888206621734610397L;

    public InvalidCcdCaseDataException(final String message) {
        super(message);
    }
}
