package uk.gov.hmcts.sptribs.testutil;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseDataReferenceService {

    private final JdbcTemplate jdbcTemplate;

    public CaseDataReferenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void ensureReferenceExists(long caseReference) {
        jdbcTemplate.update(
            """
                INSERT INTO ccd.case_data(
                    id,
                    reference,
                    security_classification,
                    jurisdiction,
                    case_type_id,
                    state,
                    data
                )
                VALUES (
                    ?,
                    ?,
                    'PUBLIC',
                    'ST_CIC',
                    'CriminalInjuriesCompensation',
                    'Draft',
                    '{}'::jsonb
                )
                ON CONFLICT (reference) DO NOTHING
                """,
            caseReference,
            caseReference
        );
    }
}
