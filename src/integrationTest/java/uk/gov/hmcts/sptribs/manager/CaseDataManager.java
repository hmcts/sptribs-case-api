package uk.gov.hmcts.sptribs.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

@TestComponent
public class CaseDataManager implements IntegrationTestDataManager {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void addCaseData(Long reference, String state, String json) {
        jdbcTemplate.update("""
                INSERT INTO ccd.case_data (id, reference, jurisdiction, case_type_id, state, data, security_classification)
                VALUES (1, :reference, 'ST_CIC', 'CriminalInjuriesCompensation', :state, CAST(:data AS jsonb),
                CAST('PUBLIC' AS ccd.securityclassification))
                """, Map.of(
            "reference", reference,
            "state", state,
            "data", json
        ));
    }

    @Override
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM ccd.case_data", Map.of());
    }
}
