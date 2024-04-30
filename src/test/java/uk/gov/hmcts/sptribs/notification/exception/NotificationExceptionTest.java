package uk.gov.hmcts.sptribs.notification.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class NotificationExceptionTest {

    @Test
    void shouldthrowNotificationExceptionTest() {

        String message = "Failed to send Notification";
        Exception cause = new RuntimeException(message);

        NotificationException notificationException = new NotificationException(cause);

        assertEquals(cause, notificationException.getCause());
        assertTrue(notificationException.getMessage().contains(message));
    }


}
