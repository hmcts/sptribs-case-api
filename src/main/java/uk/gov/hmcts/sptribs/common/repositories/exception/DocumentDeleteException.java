package uk.gov.hmcts.sptribs.common.repositories.exception;

public class DocumentDeleteException extends RuntimeException {
    public DocumentDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
