package uk.gov.hmcts.sptribs.notifyproxy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GovNotifyService {

    public static final int ONE_HOUR = 1000 * 60 * 60;

    @Scheduled(fixedRate = ONE_HOUR)
    public void NotifyAtIntervals() {
        log.info("Calling gov notify");

    }
}
