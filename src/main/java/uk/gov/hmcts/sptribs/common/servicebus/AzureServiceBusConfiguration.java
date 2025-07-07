package uk.gov.hmcts.sptribs.common.servicebus;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "spring.jms.servicebus.enabled", havingValue = "true")
@Import(com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsAutoConfiguration.class)
public class AzureServiceBusConfiguration {
}
