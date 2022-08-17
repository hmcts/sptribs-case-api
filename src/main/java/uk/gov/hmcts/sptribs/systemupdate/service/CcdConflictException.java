package uk.gov.hmcts.sptribs.systemupdate.service;

public class CcdConflictException extends RuntimeException {

    private static final long serialVersionUID = -7177902742592517612L;

    public CcdConflictException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
