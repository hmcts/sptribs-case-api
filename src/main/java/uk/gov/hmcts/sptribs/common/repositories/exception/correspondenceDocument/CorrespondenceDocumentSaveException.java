package uk.gov.hmcts.sptribs.common.repositories.exception.correspondenceDocument;

public class CorrespondenceDocumentSaveException extends RuntimeException {
    public CorrespondenceDocumentSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
