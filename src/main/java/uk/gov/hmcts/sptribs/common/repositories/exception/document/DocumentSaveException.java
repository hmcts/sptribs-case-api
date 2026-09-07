package uk.gov.hmcts.sptribs.common.repositories.exception.document;

public class DocumentSaveException extends RuntimeException {
    public DocumentSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
