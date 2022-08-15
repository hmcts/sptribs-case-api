package uk.gov.hmcts.sptribs.notification.exception;

public class NotificationException extends RuntimeException {
    private static final long serialVersionUID = 5604833464289587151L;

    public NotificationException(Exception cause) {
        super(cause);
    }
}
