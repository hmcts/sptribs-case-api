package uk.gov.hmcts.sptribs.common.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.jms.servicebus.enabled", havingValue = "true")
public class CcdCaseEventPublisher {

    @Value("${azure.servicebus.ccd-case-events.topic-name}")
    private String topicName;

    @Autowired
    private JmsTemplate jmsTemplate;

    public void publishMessage(String message) {
        try {
            log.info("Attempting to send message to Azure Service Bus topic: {}", topicName);
            jmsTemplate.convertAndSend("ccd-case-events-aat", message);
            log.info("Message sent successfully to Azure Service Bus topic: {}", topicName);
        } catch (Exception e) {
            log.error("Error sending message to Azure Service Bus topic {}: {}", topicName, e.getMessage(), e);
        }
    }
}
