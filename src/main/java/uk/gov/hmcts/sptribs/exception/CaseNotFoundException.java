package uk.gov.hmcts.sptribs.exception;

import java.io.Serial;

public class CaseNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CaseNotFoundException(String message) {
        super(message);
    }
}




