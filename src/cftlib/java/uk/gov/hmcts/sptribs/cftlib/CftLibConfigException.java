package uk.gov.hmcts.sptribs.cftlib;

public class CftLibConfigException extends RuntimeException {

    private static final long serialVersionUID = 8105127894487074518L;

    public CftLibConfigException(final String message) {
        super(message);
    }

    public CftLibConfigException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
