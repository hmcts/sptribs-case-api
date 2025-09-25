package uk.gov.hmcts.sptribs.common.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(
    name = {"spring.jms.servicebus.enabled", "sptribs.servicebus.scheduler-enabled"},
    havingValue = "true"
)
public class CcdCaseEventScheduler {

    @Autowired
    private CcdCaseEventPublisher ccdCaseEventPublisher;

    @Scheduled(fixedRate = 30000) // 30 seconds
    public void publishTestMessage() {
        String testMessage = """
            {
                "caseId": "1234567890123456",
                "eventType": "TEST_EVENT",
                "timestamp": "%d"
            }
            """.formatted(System.currentTimeMillis());
        log.info("Scheduled task: Publishing test message to CCD Case Events Service Bus.");
        ccdCaseEventPublisher.publishMessage(testMessage);
    }
}
