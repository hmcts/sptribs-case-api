package uk.gov.hmcts.sptribs.common.servicebus;

import lombok.extern.slf4j.Slf4j;
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

    private final CcdCaseEventPublisher ccdCaseEventPublisher;

    public CcdCaseEventScheduler(CcdCaseEventPublisher ccdCaseEventPublisher) {
        this.ccdCaseEventPublisher = ccdCaseEventPublisher;
    }

    @Scheduled(cron = "${sptribs.servicebus.schedule:*/30 * * * * *}")
    public void publishPendingMessages() {
        log.debug("Scheduled CCD case event publishing task invoked");
        ccdCaseEventPublisher.publishPendingCaseEvents();
    }
}
