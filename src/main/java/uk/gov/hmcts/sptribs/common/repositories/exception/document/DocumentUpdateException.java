package uk.gov.hmcts.sptribs.common.repositories.exception.document;

public class DocumentUpdateException extends RuntimeException {
    public DocumentUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
