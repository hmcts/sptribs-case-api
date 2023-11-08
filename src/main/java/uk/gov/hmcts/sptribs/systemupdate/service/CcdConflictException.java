package uk.gov.hmcts.sptribs.systemupdate.service;

public class CcdConflictException extends RuntimeException {

    public CcdConflictException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
