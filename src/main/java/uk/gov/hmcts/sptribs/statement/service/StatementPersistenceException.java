package uk.gov.hmcts.sptribs.statement.service;

public class StatementPersistenceException extends RuntimeException {

    public StatementPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
