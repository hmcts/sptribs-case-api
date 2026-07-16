package uk.gov.hmcts.sptribs.systemupdate;

import org.apache.logging.log4j.core.config.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Order(1)
public class MigrationSecurityConfiguration {

    @Bean
    public SecurityFilterChain migrationFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/migration/run")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
