package uk.gov.hmcts.sptribs.testutil;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.ccd.sdk.taskmanagement.TaskOutboxService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.Mockito.mock;

@Configuration
public class TaskManagementTestConfiguration {

    @Bean
    public TaskOutboxService taskOutboxService() {
        return mock(TaskOutboxService.class);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "s2s.stub", havingValue = "true")
    public AuthTokenGenerator stubAuthTokenGenerator() {
        return () -> "Bearer stub-s2s-token";
    }
}

