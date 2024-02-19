package uk.gov.hmcts.sptribs.notification.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class NotificationTemplateExceptionTest {

    @Test
    void shouldThrowNotificationTemplateException() {

        String message = "Failed to send Notification";

        NotificationTemplateException notificationTemplateException = new NotificationTemplateException(message);

        assertEquals(message, notificationTemplateException.getMessage());
    }
}
