package uk.gov.hmcts.sptribs.common.repositories.exception.document;

public class DocumentLookupException extends RuntimeException {
    public DocumentLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
