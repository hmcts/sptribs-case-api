package uk.gov.hmcts.sptribs.testutil.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

/** Removes the central CCD pointer and search projections for explicitly registered test cases only. */
@Component
@Profile("functional")
public class CcdCaseCleanup {
    private static final String CASE_TYPE = "CriminalInjuriesCompensation";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${test-case-cleanup.enabled:false}")
    private boolean enabled;
    @Value("${test-case-cleanup.ccd-jdbc-url:}")
    private String jdbcUrl;
    @Value("${test-case-cleanup.ccd-username:}")
    private String username;
    @Value("${test-case-cleanup.ccd-password:}")
    private String password;
    @Value("${test-case-cleanup.elasticsearch-url:}")
    private String elasticsearchUrl;

    public boolean isEnabled() {
        return enabled;
    }

    public void validateConfiguration() {
        if (enabled && (jdbcUrl.isBlank() || username.isBlank() || elasticsearchUrl.isBlank())) {
            throw new IllegalStateException("Test cleanup requires CCD JDBC URL, username and Elasticsearch URL");
        }
    }

    public void deleteCcdPointer(long reference) throws SQLException {
        try (Connection ccd = DriverManager.getConnection(jdbcUrl, username, password)) {
            ccd.setAutoCommit(false);
            try {
                deletePointer(ccd, reference);
                ccd.commit();
            } catch (SQLException | RuntimeException exception) {
                ccd.rollback();
                throw exception;
            }
        }
    }

    // CCD IDs are independent of the IDs in the decentralised service database.
    void deletePointer(Connection ccd, long reference) throws SQLException {
        try (PreparedStatement select = ccd.prepareStatement(
            "SELECT id, case_type_id FROM public.case_data WHERE reference = ? FOR UPDATE")) {
            select.setLong(1, reference);
            try (ResultSet result = select.executeQuery()) {
                if (!result.next()) {
                    return; // Idempotent retry after a successful database deletion.
                }
                if (!CASE_TYPE.equals(result.getString("case_type_id"))) {
                    throw new IllegalStateException("Refusing cleanup of another case type for " + reference);
                }
                long id = result.getLong("id");
                execute(ccd, "DELETE FROM public.case_link WHERE case_id = ? OR linked_case_id = ?", id, id);
                execute(ccd, "DELETE FROM public.case_users_audit WHERE case_data_id = ?", id);
                execute(ccd, "DELETE FROM public.case_users WHERE case_data_id = ?", id);
                execute(ccd, "DELETE FROM public.case_event_significant_items WHERE case_event_id IN "
                    + "(SELECT id FROM public.case_event WHERE case_data_id = ?)", id);
                execute(ccd, "DELETE FROM public.case_event WHERE case_data_id = ?", id);
                execute(ccd, "DELETE FROM public.case_data WHERE id = ?", id);
            }
        }
    }

    static void execute(Connection database, String sql, long... values) throws SQLException {
        try (PreparedStatement statement = database.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setLong(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }

    public void deleteSearchProjections(long reference) {
        // Delete repeatedly: Logstash may have read the case before the database transaction committed.
        // A quiet window is bounded convergence, not a distributed transaction with Logstash.
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        long quietSince = System.nanoTime();
        do {
            long deleted = deleteSearchProjection(reference);
            if (deleted > 0 || countSearchProjections(reference) > 0) {
                quietSince = System.nanoTime();
            } else if (System.nanoTime() - quietSince >= Duration.ofSeconds(10).toNanos()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted during case cleanup", exception);
            }
        } while (System.nanoTime() < deadline);
        throw new IllegalStateException("Case keeps reappearing in Elasticsearch: " + reference);
    }

    long deleteSearchProjection(long reference) {
        JsonNode response = request("_delete_by_query?refresh=true", reference);
        if (!response.path("deleted").isIntegralNumber() || response.path("deleted").asLong() < 0
            || !response.has("failures") || !response.get("failures").isArray()
            || !response.get("failures").isEmpty() || response.path("timed_out").asBoolean(true)
            || response.path("version_conflicts").asLong(-1) != 0) {
            throw new IllegalStateException("Elasticsearch deletion failed for " + reference + ": " + response);
        }
        return response.get("deleted").longValue();
    }

    long countSearchProjections(long reference) {
        JsonNode response = request("_count", reference);
        if (!response.path("count").isIntegralNumber() || response.path("count").asLong() < 0
            || response.path("_shards").path("failed").asInt(-1) != 0) {
            throw new IllegalStateException("Elasticsearch verification failed for " + reference + ": " + response);
        }
        return response.get("count").longValue();
    }

    private JsonNode request(String operation, long reference) {
        // Explicit indices avoid touching unrelated services. Missing indices are allowed for fresh previews.
        URI uri = URI.create(elasticsearchUrl.replaceAll("/$", "")
            + "/criminalinjuriescompensation_cases,global_search/" + operation
            + (operation.contains("?") ? "&" : "?") + "ignore_unavailable=true&allow_no_indices=true");
        String query = "{\"query\":{\"term\":{\"reference.keyword\":\"" + reference + "\"}}}";
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(query)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Elasticsearch HTTP " + response.statusCode() + " for " + reference
                    + ": " + response.body());
            }
            return MAPPER.readTree(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during Elasticsearch cleanup", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot clean Elasticsearch case " + reference, exception);
        }
    }
}
