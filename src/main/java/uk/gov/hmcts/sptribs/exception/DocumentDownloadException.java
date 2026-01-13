package uk.gov.hmcts.sptribs.exception;

import java.io.Serial;

public class DocumentDownloadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8541970807219456969L;

    public DocumentDownloadException(String message) {
        super(message);
    }

    public DocumentDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}


