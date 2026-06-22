package uk.gov.hmcts.sptribs.statement.service;

public class StatementValidationException extends RuntimeException {

    public StatementValidationException(String message) {
        super(message);
    }
}
