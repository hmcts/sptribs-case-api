package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;

import java.util.Map;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaseDataRepositoryImplIT {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withInitScript("sql/create_test_case_data_table.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name",
            () -> "org.postgresql.Driver");
    }

    @Autowired
    private CaseDataRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnTrueWhenCaseExistsAndValidState() {
        System.out.println(postgres.getJdbcUrl());
        insertCase("Submitted", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenStateIsInvalid() {
        insertCase("Draft", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isFalse();
    }

    @Test
    void shouldFindCaseByDirectEmail() {
        insertCase("Submitted", """
        {
          "cicCaseEmail": "test@example.com"
        }
    """);

        var result = repository.findCase("123", "test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void shouldFindCaseBySearchPartiesEmail() {
        insertCase("Submitted", """
        {
          "SearchCriteria": {
            "SearchParties": [
              {
                "value": {
                  "EmailAddress": "test@example.com"
                }
              }
            ]
          }
        }
    """);

        var result = repository.findCase("123", "test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotMatch() {
        insertCase("Submitted", "{}");

        var result = repository.findCase("123", "wrong@example.com");

        assertThat(result).isEmpty();
    }

    private void insertCase(String state, String json) {
        jdbcTemplate.update("""
        INSERT INTO ccd.case_data (reference, jurisdiction, case_type_id, state, data, last_modified)
        VALUES (:reference, 'ST_CIC', 'CriminalInjuriesCompensation', :state, :data::jsonb, now())
    """, Map.of(
            "reference", Long.valueOf("123"),
            "state", state,
            "data", json
        ));
    }
}
