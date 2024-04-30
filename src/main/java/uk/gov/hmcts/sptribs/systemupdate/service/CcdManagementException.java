package uk.gov.hmcts.sptribs.systemupdate.service;

import lombok.Getter;

@Getter
public class CcdManagementException extends RuntimeException {

    private int status;

    public CcdManagementException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public CcdManagementException(final int status, final String message, final Throwable throwable) {
        super(message, throwable);
        this.status = status;
    }

}
