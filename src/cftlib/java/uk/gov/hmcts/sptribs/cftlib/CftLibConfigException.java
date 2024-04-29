package uk.gov.hmcts.sptribs.cftlib;

import java.io.Serial;

public class CftLibConfigException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8105127894487074518L;

    public CftLibConfigException(final String message) {
        super(message);
    }

    public CftLibConfigException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
