package uk.gov.hmcts.sptribs.testutil;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_CORRESPONDENCES_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_DATA_ID;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_DATA_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_EVENT_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_CORRESPONDENCES;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_DATA;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_EVENT;

@Component
@Profile("functional")
public class FunctionalTestDataManager {

    private static final Logger log = LoggerFactory.getLogger(FunctionalTestDataManager.class);

    @Value("${postgres.host}")
    private String host;

    @Value("${postgres.port}")
    private String port;

    @Value("${postgres.username}")
    private String username;

    @Value("${postgres.name}")
    private String dbName;

    @Value("${postgres.password}")
    private String password;

    public void connectToDB() {
        try (Connection connection = createConnection()) {
            log.info("Successfully connected to database: {}", connectionString());
        } catch (SQLException e) {
            log.error("Failed to establish database connection to {}.", connectionString(), e);
            throw new RuntimeException("Failed to establish database connection to: " + connectionString(), e);
        }
    }

    public void clearDown(long reference) throws SQLException {
        log.info("Starting clearDown for reference: {}", reference);

        try (Connection connection = createConnection()) {
            deleteCaseEvent(connection, reference);
            deleteCaseData(connection, reference);
            deleteCaseCorrespondences(connection, reference);
        }
        deleteCaseFromElasticsearch(reference);

        log.info("Clear down completed for reference: {}", reference);
    }

    public void deleteCaseData(Connection connection, long reference) {
        deleteFromTable(connection, TABLE_CASE_DATA, KEY_CASE_DATA_REFERENCE, reference);
    }

    public void deleteCaseCorrespondences(Connection connection, long reference) {
        deleteFromTable(connection, TABLE_CASE_CORRESPONDENCES, KEY_CASE_CORRESPONDENCES_REFERENCE, reference);
    }

    public void deleteCaseEvent(Connection connection, long reference) {
        String sql = "DELETE FROM " + TABLE_CASE_EVENT
            + " WHERE " + KEY_CASE_EVENT_REFERENCE + " IN ("
            + "SELECT " + KEY_CASE_DATA_ID + " FROM " + TABLE_CASE_DATA + " WHERE " + KEY_CASE_DATA_REFERENCE + " = ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reference);
            int rowsAffected = statement.executeUpdate();
            log.info("Deleted {} row(s) for case reference {} from table {}.",
                rowsAffected, reference, TABLE_CASE_EVENT);
        } catch (SQLException e) {
            log.error("Error deleting case events for case reference {}.", reference, e);
            throw new RuntimeException("Failed to delete case events for case reference: " + reference, e);
        }
    }

    private void deleteFromTable(Connection connection, String table, String column, long reference) {
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

    private String connectionString() {
        return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(connectionString(), username, password);
    }

    public void deleteCaseFromElasticsearch(long reference) {
        String elasticsearchBaseUrl = "http://localhost:9200";
        String caseIdStr = String.valueOf(reference);
        String deleteUrl = elasticsearchBaseUrl + "/*_cases/_delete_by_query?ignore_unavailable=true&refresh=true";

        String jsonPayload = "{\n"
            + "  \"query\": {\n"
            + "    \"bool\": {\n"
            + "      \"should\": [\n"
            + "        { \"term\": { \"reference\": " + reference + " } },\n"
            + "        { \"term\": { \"reference\": \"" + reference + "\" } },\n"
            + "        { \"term\": { \"id\": " + reference + " } }\n"
            + "      ]\n"
            + "    }\n"
            + "  }\n"
            + "}";

        try {
            Response response = RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Content-Type", "application/json")
                .body(jsonPayload)
                .when()
                .post(deleteUrl);

            int statusCode = response.getStatusCode();
            log.info("Elasticsearch response status code: {}, body: {}", statusCode, response.getBody().asString());
        } catch (Exception e) {
            log.info("Error occurred while deleting case {} from Elasticsearch", caseIdStr, e);
        }
    }

    public void closeAll() {
        log.debug("No shared database connection to close.");
    }
}
