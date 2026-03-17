package uk.gov.hmcts.sptribs.common.repositories.exception;

public class CaseEventRepositoryException extends RuntimeException {
    public CaseEventRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
