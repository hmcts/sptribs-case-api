package uk.gov.hmcts.sptribs.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotificationsConfiguration {
    @Bean
    public NotificationClient notificationClient(
        @Value("${uk.gov.notify.api.sptribskey}") String sptribsApiKey,
        @Value("${uk.gov.notify.api.baseUrl}") final String baseUrl
    ) {
        return new NotificationClient(sptribsApiKey, baseUrl);
    }
}
