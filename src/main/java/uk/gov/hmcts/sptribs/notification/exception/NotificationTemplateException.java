package uk.gov.hmcts.sptribs.notification.exception;

import java.io.Serial;

public class NotificationTemplateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3160179487262446549L;

    public NotificationTemplateException(final String message) {
        super(message);
    }
}
