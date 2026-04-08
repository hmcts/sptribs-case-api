package uk.gov.hmcts.sptribs.testsupport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@ConditionalOnProperty(prefix = "test-support", name = "enabled", havingValue = "true")
public class FunctionalTestSupportController {

    private static final String UPSERT_CASE_DATA = """
        INSERT INTO ccd.case_data
            (id, jurisdiction, case_type_id, state, data, reference, security_classification)
        VALUES (:caseReference, 'ST_CIC', 'CriminalInjuriesCompensation', 'Draft', '{}'::jsonb, :caseReference, 'PUBLIC')
        ON CONFLICT (reference) DO UPDATE
            SET id = EXCLUDED.id
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public FunctionalTestSupportController(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/testing-support/seed-case-reference/{caseReference}")
    public ResponseEntity<Void> seedCaseReference(@PathVariable long caseReference) {
        jdbcTemplate.update(UPSERT_CASE_DATA, new MapSqlParameterSource("caseReference", caseReference));
        log.debug("Ensured ccd.case_data reference {} exists for functional tests", caseReference);
        return ResponseEntity.noContent().build();
    }
}
