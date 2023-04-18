package uk.gov.hmcts.sptribs.exception;

public class DocumentUploadOrDeleteException extends RuntimeException {
    private static final long serialVersionUID = 7442994120484411078L;

    public DocumentUploadOrDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
