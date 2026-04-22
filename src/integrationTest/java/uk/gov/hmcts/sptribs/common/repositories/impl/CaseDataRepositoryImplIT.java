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
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;

import java.util.Map;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaseDataRepositoryImplIT extends IntegrationTestBase {

//    @Container
//    static PostgreSQLContainer<?> postgres =
//        new PostgreSQLContainer<>("postgres:16")
//            .withInitScript("sql/create_test_case_data_table.sql");
//
//    @DynamicPropertySource
//    static void props(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.datasource.driver-class-name",
//            () -> "org.postgresql.Driver");
//    }

    @Autowired
    private CaseDataRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void givenCCDReference_thenShouldReturnTrueWhenCaseExistsAndValidState() {
        insertCase("Submitted", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isTrue();
    }

    @Test
    void givenCCDReference_thenShouldReturnFalseWhenStateIsInvalid() {
        insertCase("Draft", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isFalse();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseByEmail() {
        insertCase("Submitted", """
                {
                  "cicCaseEmail": "test@example.com"
                }
            """);

        var result = repository.findCase("123", "test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseBySearchPartiesEmail() {
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
    void givenCCDReference_thenShouldReturnEmptyWhenEmailDoesNotMatch() {
        insertCase("Submitted", "{}");

        var result = repository.findCase("123", "wrong@example.com");

        assertThat(result).isEmpty();
    }

    private void insertCase(String state, String json) {
        jdbcTemplate.update("""
                INSERT INTO ccd.case_data (id, reference, jurisdiction, case_type_id, state, data,
                                           security_classification, last_modified)
                VALUES (1, :reference, 'ST_CIC', 'CriminalInjuriesCompensation', :state, :data::jsonb,
                        :securityClassification, now())
            """, Map.of(
            "reference", Long.valueOf("123"),
            "state", state,
            "data", json,
            "securityClassification", SecurityClassification.PUBLIC.name()
        ));
    }
}
