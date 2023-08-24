package uk.gov.hmcts.sptribs.notification.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IdamNotificationException extends RuntimeException {

    private String server;

    private HttpStatus status;

    public static final long serialVersionUID = 336287436;

    public IdamNotificationException(String server, HttpStatus status, Throwable cause) {
        super(cause);
        this.server = server;
        this.status = status;
    }
}
