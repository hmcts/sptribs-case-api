package uk.gov.hmcts.sptribs.common.config;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfiguration {
    public class SchedulerConfig {

        @Bean
        public LockProvider lockProvider(DataSource dataSource) {
            return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                    .withJdbcTemplate(new JdbcTemplate(dataSource))
                    .usingDbTime() // Use database time instead of application time
                    .build()
            );
        }
    }
}
