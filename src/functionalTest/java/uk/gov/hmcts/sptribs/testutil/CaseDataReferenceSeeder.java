package uk.gov.hmcts.sptribs.testutil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
@Slf4j
public class CaseDataReferenceSeeder {

    private static final String SPTRIBS_JDBC_URL =
        System.getenv().getOrDefault("FT_SPTRIBS_JDBC_URL", "jdbc:postgresql://localhost:6432/sptribs");
    private static final String SPTRIBS_DB_USER =
        System.getenv().getOrDefault("FT_SPTRIBS_DB_USER", "postgres");
    private static final String SPTRIBS_DB_PASSWORD =
        System.getenv().getOrDefault("FT_SPTRIBS_DB_PASSWORD", "");

    private static final String UPSERT_CASE_DATA = """
        INSERT INTO ccd.case_data
            (id, jurisdiction, case_type_id, state, data, reference, security_classification)
        VALUES (?, 'ST_CIC', 'CriminalInjuriesCompensation', 'Draft', '{}'::jsonb, ?, 'PUBLIC')
        ON CONFLICT (reference) DO UPDATE
            SET id = EXCLUDED.id
        """;

    public void ensureCaseDataReferenceExists(long caseReference) {
        try (Connection connection = DriverManager.getConnection(
            SPTRIBS_JDBC_URL,
            SPTRIBS_DB_USER,
            SPTRIBS_DB_PASSWORD
        ); PreparedStatement statement = connection.prepareStatement(UPSERT_CASE_DATA)) {
            statement.setLong(1, caseReference);
            statement.setLong(2, caseReference);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to seed case_data reference for functional tests", exception);
        }

        log.debug("Ensured sptribs ccd.case_data row exists for reference {}", caseReference);
    }
}
