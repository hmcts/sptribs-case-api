package uk.gov.hmcts.sptribs.testutil.data;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CcdCaseCleanupTest {
    private CcdCaseCleanup cleanup;
    private Connection database;
    private HttpServer server;
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    @BeforeEach
    void setUp() throws SQLException {
        cleanup = new CcdCaseCleanup();
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        database = DriverManager.getConnection(url, "sa", "");
        ReflectionTestUtils.setField(cleanup, "enabled", true);
        ReflectionTestUtils.setField(cleanup, "jdbcUrl", url);
        ReflectionTestUtils.setField(cleanup, "username", "sa");
        ReflectionTestUtils.setField(cleanup, "password", "");
        sql("CREATE TABLE case_data (id BIGINT PRIMARY KEY, reference BIGINT UNIQUE, case_type_id VARCHAR)");
        sql("CREATE TABLE case_event (id BIGINT PRIMARY KEY, case_data_id BIGINT REFERENCES case_data(id))");
        sql("CREATE TABLE case_event_significant_items (case_event_id BIGINT REFERENCES case_event(id))");
        sql("CREATE TABLE case_users (case_data_id BIGINT)");
        sql("CREATE TABLE case_users_audit (case_data_id BIGINT)");
        sql("CREATE TABLE case_link (case_id BIGINT REFERENCES case_data(id), "
            + "linked_case_id BIGINT REFERENCES case_data(id))");
        sql("INSERT INTO case_data VALUES (10, 1234567890123456, 'CriminalInjuriesCompensation'), "
            + "(20, 2234567890123456, 'CriminalInjuriesCompensation')");
        sql("INSERT INTO case_event VALUES (100, 10), (200, 20)");
        sql("INSERT INTO case_event_significant_items VALUES (100), (200)");
        sql("INSERT INTO case_users VALUES (10), (20)");
        sql("INSERT INTO case_users_audit VALUES (10), (20)");
        sql("INSERT INTO case_link VALUES (10, 20), (20, 10), (20, 20)");
    }

    @AfterEach
    void close() throws SQLException {
        if (server != null) {
            server.stop(0);
        }
        sql("DROP ALL OBJECTS");
        database.close();
    }

    @Test
    void deletesOnlyRegisteredCaseAndLinksInBothDirections() throws SQLException {
        cleanup.deleteCcdPointer(1234567890123456L);
        cleanup.deleteCcdPointer(1234567890123456L);
        assertEquals(1, count("case_data"));
        assertEquals(1, count("case_event"));
        assertEquals(1, count("case_event_significant_items"));
        assertEquals(1, count("case_users"));
        assertEquals(1, count("case_users_audit"));
        assertEquals(1, count("case_link"));
    }

    @Test
    void rollsBackAllCentralDeletesOnFailure() throws SQLException {
        sql("CREATE TABLE blocker (case_id BIGINT REFERENCES case_data(id))");
        sql("INSERT INTO blocker VALUES (10)");
        assertThrows(SQLException.class, () -> cleanup.deleteCcdPointer(1234567890123456L));
        assertEquals(2, count("case_data"));
        assertEquals(2, count("case_event"));
        assertEquals(2, count("case_users"));
        assertEquals(3, count("case_link"));
    }

    @Test
    void refusesAnotherCaseType() throws SQLException {
        sql("UPDATE case_data SET case_type_id = 'Other' WHERE id = 10");
        assertThrows(IllegalStateException.class, () -> cleanup.deleteCcdPointer(1234567890123456L));
        assertEquals(2, count("case_data"));
    }

    @Test
    void requiresExplicitTargetsWhenEnabled() {
        ReflectionTestUtils.setField(cleanup, "elasticsearchUrl", "");
        assertThrows(IllegalStateException.class, cleanup::validateConfiguration);
    }

    @Test
    void deletesUsingExactReferenceAcrossBothIndices() throws Exception {
        respond(200, "{\"deleted\":2,\"failures\":[],\"timed_out\":false,\"version_conflicts\":0}");
        assertEquals(2, cleanup.deleteSearchProjection(1234567890123456L));
        assertTrue(requestPath.get().startsWith("/criminalinjuriescompensation_cases,global_search/_delete_by_query?"));
        assertFalse(requestPath.get().contains("*_cases"));
        assertEquals("{\"query\":{\"term\":{\"reference.keyword\":\"1234567890123456\"}}}", requestBody.get());
    }

    @Test
    void failsOnHttpErrors() throws Exception {
        respond(403, "{\"error\":\"forbidden\"}");
        assertThrows(IllegalStateException.class, () -> cleanup.deleteSearchProjection(1234567890123456L));
    }

    @Test
    void failsOnPartialDeleteDespiteHttpSuccess() throws Exception {
        respond(200, "{\"deleted\":1,\"failures\":[{}],\"timed_out\":false,\"version_conflicts\":0}");
        assertThrows(IllegalStateException.class, () -> cleanup.deleteSearchProjection(1234567890123456L));
    }

    @Test
    void failsOnVersionConflicts() throws Exception {
        respond(200, "{\"deleted\":0,\"failures\":[],\"timed_out\":false,\"version_conflicts\":1}");
        assertThrows(IllegalStateException.class, () -> cleanup.deleteSearchProjection(1234567890123456L));
    }

    @Test
    void failsOnIncompleteCountResponse() throws Exception {
        respond(200, "{\"count\":0,\"_shards\":{\"failed\":1}}");
        assertThrows(IllegalStateException.class, () -> cleanup.countSearchProjections(1234567890123456L));
    }

    @Test
    void verifiesSearchAbsence() throws Exception {
        respond(200, "{\"count\":0,\"_shards\":{\"failed\":0}}");
        assertEquals(0, cleanup.countSearchProjections(1234567890123456L));
    }

    @Test
    void removesDelayedIndexWriteBeforeAcceptingAQuietWindow() {
        AtomicInteger attempts = new AtomicInteger();
        CcdCaseCleanup delayedWrite = new CcdCaseCleanup() {
            @Override
            long deleteSearchProjection(long reference) {
                // Initially absent, then an already-fetched Logstash event arrives.
                return attempts.incrementAndGet() == 2 ? 1 : 0;
            }

            @Override
            long countSearchProjections(long reference) {
                return 0;
            }
        };
        delayedWrite.deleteSearchProjections(1234567890123456L);
        assertTrue(attempts.get() >= 12, "The delayed write must restart the ten-second quiet window");
    }

    private void respond(int status, String body) throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestPath.set(exchange.getRequestURI().toString());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        ReflectionTestUtils.setField(cleanup, "elasticsearchUrl", "http://127.0.0.1:" + server.getAddress().getPort());
    }

    private void sql(String sql) throws SQLException {
        try (var statement = database.createStatement()) {
            statement.execute(sql);
        }
    }

    private int count(String table) throws SQLException {
        try (var statement = database.createStatement(); var result = statement.executeQuery("SELECT count(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }
}
