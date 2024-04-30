package uk.gov.hmcts.sptribs.systemupdate.service;

import java.io.Serial;

public class CcdSearchCaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1226241000163434323L;

    public CcdSearchCaseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
