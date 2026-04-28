package uk.gov.hmcts.sptribs.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_CORRESPONDENCES_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_DATA_ID;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_DATA_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_EVENT_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_CORRESPONDENCES;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_DATA;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_EVENT;

public class FunctionalTestDataManager {

    private static final Logger log = LoggerFactory.getLogger(FunctionalTestDataManager.class);

    private static final List<Long> testReferences = Collections.synchronizedList(new ArrayList<>());

    private final Connection connection;

    private FunctionalTestDataManager(Connection connection) {
        this.connection = connection;
    }

    public static FunctionalTestDataManager connectToDB() {

        String host = required("DB_HOST");
        String port = required("DB_PORT");
        String username = required("DB_USERNAME");
        String dbName = required("DB_NAME");
        String password = "DB_PASSWORD";

        String connectionString = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

        try {
            Connection connection = DriverManager.getConnection(connectionString, username, password);

            return new FunctionalTestDataManager(connection);
        } catch (SQLException e) {
            log.error("Failed to establish database connection to {}.", connectionString, e);
            throw new RuntimeException("Failed to establish database connection to: " + connectionString, e);
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required environment variable '" + name + "' is not set or is blank.");
        }
        return value;
    }

    public void clearDown(long reference) throws SQLException {
        log.info("Starting clearDown for reference: {}", reference);

        deleteCaseEvent(reference);
        deleteCaseData(reference);
        deleteCaseCorrespondences(reference);

        log.info("Clear down completed for reference: {}", reference);
    }

    public void deleteCaseData(long reference) {
        deleteFromTable(TABLE_CASE_DATA, KEY_CASE_DATA_REFERENCE, reference);
    }

    public void deleteCaseCorrespondences(long reference) {
        deleteFromTable(TABLE_CASE_CORRESPONDENCES, KEY_CASE_CORRESPONDENCES_REFERENCE, reference);
    }

    public void deleteCaseEvent(long reference) throws SQLException {

        if (getCaseDataId(reference) != -1) {
            long caseDataId = getCaseDataId(reference);
            deleteFromTable(TABLE_CASE_EVENT, KEY_CASE_EVENT_REFERENCE, caseDataId);
        }
    }

    private void deleteFromTable(String table, String column, long reference) {
        String sql = "DELETE FROM " + table + " WHERE " + column + " = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reference);
            int rowsAffected = statement.executeUpdate();
            log.info("Deleted {} row(s) for reference {} from table {}.",
                rowsAffected, reference, table);
        } catch (SQLException e) {
            log.error("Error deleting reference {} from table {}.", reference, table, e);
            throw new RuntimeException(
                "Failed to delete from " + table + " for reference: " + reference, e);
        }
    }

    private long getCaseDataId(long reference) throws SQLException {
        String sql = "SELECT " + KEY_CASE_DATA_ID + " FROM " + TABLE_CASE_DATA + " WHERE " + KEY_CASE_DATA_REFERENCE + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, reference);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(KEY_CASE_DATA_ID);
            }
            return -1;
        }
    }

    public void addReference(Long id) {
        testReferences.add(id);
    }

    public List<Long> getTestReferences() {
        return Collections.unmodifiableList(testReferences);
    }

    public void closeAll() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    log.info("Database connection closed.");
                }
            } catch (SQLException e) {
                log.error("Error while closing the database connection.", e);
            }
        }
    }
}
