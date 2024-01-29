package uk.gov.hmcts.sptribs.exception;

import java.io.Serial;

public class DocumentUploadOrDeleteException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7442994120484411078L;

    public DocumentUploadOrDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
