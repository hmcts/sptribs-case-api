package uk.gov.hmcts.sptribs.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static final List<Long> testReferences = Collections.synchronizedList(new ArrayList<>());

    private Connection connection;

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

    @Value("${core_case_data.api.url}")
    private String ccdDataStoreUrl;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    public void connectToDB() {
        String connectionString = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

        try {
            connection = DriverManager.getConnection(connectionString, username, password);
            log.info("Successfully connected to database: {}", connectionString);
        } catch (SQLException e) {
            log.error("Failed to establish database connection to {}.", connectionString, e);
            throw new RuntimeException("Failed to establish database connection to: " + connectionString, e);
        }
    }

    public void clearDown(long reference) throws SQLException {
        log.info("Starting clearDown for reference: {}", reference);

        cleanDatabaseViaCcdEndpoint();
        deleteCaseEvent(reference);
        deleteCaseData(reference);
        deleteCaseCorrespondences(reference);
        deleteCaseFromElasticsearch(reference);

        log.info("Clear down completed for reference: {}", reference);
    }

    public void cleanDatabaseViaCcdEndpoint() {
        String changeId = System.getenv("CHANGE_ID");
        if (changeId == null || changeId.isBlank()) {
            changeId = "0";
        }

        String cleanUrl = ccdDataStoreUrl + "/testing-support/cleanup-case-type/" + changeId;
        String caseTypeIds = "CriminalInjuriesCompensation";

        log.info("Sending DELETE request to CCD Data Store cleanup endpoint: {}?caseTypeIds={}", cleanUrl, caseTypeIds);
        try {
            io.restassured.response.Response response = io.restassured.RestAssured.given()
                .relaxedHTTPSValidation()
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generateCcdDataToken())
                .queryParam("caseTypeIds", caseTypeIds)
                .when()
                .delete(cleanUrl);

            log.info("CCD Data Store cleanup response status code: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Error occurred while calling CCD Data Store cleanup endpoint", e);
        }
    }

    public void deleteCaseData(long reference) {
        deleteFromTable(TABLE_CASE_DATA, KEY_CASE_DATA_REFERENCE, reference);
    }

    public void deleteCaseCorrespondences(long reference) {
        deleteFromTable(TABLE_CASE_CORRESPONDENCES, KEY_CASE_CORRESPONDENCES_REFERENCE, reference);
    }

    public void deleteCaseEvent(long reference) throws SQLException {
        long caseDataId = getCaseDataId(reference);

        if (caseDataId != -1) {
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

    private String getElasticsearchBaseUrl() {
        String hosts = System.getenv("ELASTIC_SEARCH_HOSTS");
        if (hosts == null || hosts.isBlank()) {
            hosts = System.getenv("ELASTIC_SEARCH_DATA_NODES_HOSTS");
        }
        if (hosts == null || hosts.isBlank()) {
            hosts = "http://localhost:9200";
        }
        if (!hosts.startsWith("http://") && !hosts.startsWith("https://")) {
            hosts = "http://" + hosts;
        }
        return hosts;
    }

    public void deleteCaseFromElasticsearch(long reference) {
        String elasticsearchBaseUrl = getElasticsearchBaseUrl();
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

        log.info("Sending POST request to Elasticsearch: {}", deleteUrl);
        try {
            io.restassured.response.Response response = io.restassured.RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Content-Type", "application/json")
                .body(jsonPayload)
                .when()
                .post(deleteUrl);

            int statusCode = response.getStatusCode();
            log.info("Elasticsearch response status code: {}, body: {}", statusCode, response.getBody().asString());
        } catch (Exception e) {
            log.error("Error occurred while deleting case {} from Elasticsearch", caseIdStr, e);
        }
    }

    public boolean isCaseInElasticsearch(long reference) {
        String elasticsearchBaseUrl = getElasticsearchBaseUrl();
        String searchUrl = elasticsearchBaseUrl + "/*_cases/_search?ignore_unavailable=true";

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

        log.info("Sending POST request to Elasticsearch search endpoint: {}", searchUrl);
        try {
            io.restassured.response.Response response = io.restassured.RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Content-Type", "application/json")
                .body(jsonPayload)
                .when()
                .post(searchUrl);

            int statusCode = response.getStatusCode();
            if (statusCode == 200) {
                long totalHits = response.jsonPath().getLong("hits.total.value");
                log.info("Elasticsearch search response hits: {}", totalHits);
                return totalHits > 0;
            } else {
                log.warn("Elasticsearch search response status: {}, body: {}", statusCode, response.getBody().asString());
            }
        } catch (Exception e) {
            log.error("Error occurred while searching case {} in Elasticsearch", reference, e);
        }
        return false;
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
