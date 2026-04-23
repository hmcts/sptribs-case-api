package uk.gov.hmcts.sptribs;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@TestConfiguration
class FlywayTestConfig {

    @Bean
    @Primary
    @DependsOn("ccdFlyway")
    Flyway primaryFlyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .table("flyway_schema_history")
            .schemas("public")
            .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    Flyway ccdFlyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:dataruntime-db/migration")
            .table("ccd_flyway_schema_history")
            .schemas("ccd")
            .target(MigrationVersion.fromVersion("11")) //issues with index scripts in 12
            .load();
        flyway.migrate();
        return flyway;
    }
}
