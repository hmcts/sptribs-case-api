package uk.gov.hmcts.sptribs.services.cdam;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CdamUrlDebugger {

    @Value("${case_document_am.url}")
    private String cdamUrl;

    @PostConstruct
    public void logUrls() {
        log.info("Configured Feign client URL: {}", cdamUrl);
    }
}
