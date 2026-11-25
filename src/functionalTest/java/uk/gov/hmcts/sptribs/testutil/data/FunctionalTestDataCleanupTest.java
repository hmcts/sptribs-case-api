package uk.gov.hmcts.sptribs.testutil.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FunctionalTestDataCleanupTest {
    private static final long REFERENCE = 1234567890123456L;
    private FunctionalTestDataManager manager;
    private CcdCaseCleanup projections;

    @BeforeEach
    void setUp() throws SQLException {
        manager = new FunctionalTestDataManager();
        projections = mock(CcdCaseCleanup.class);
        ReflectionTestUtils.setField(manager, "ccdCaseCleanup", projections);
        FunctionalTestDataManager.connection = DriverManager.getConnection(
            "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL", "sa", "");
        sql("CREATE SCHEMA ccd");
        sql("CREATE TABLE ccd.case_data (id BIGINT PRIMARY KEY, reference BIGINT UNIQUE)");
        sql("CREATE TABLE ccd.case_event (case_data_id BIGINT REFERENCES ccd.case_data(id))");
        sql("CREATE TABLE ccd.es_queue (reference BIGINT REFERENCES ccd.case_data(reference) ON DELETE CASCADE)");
        sql("CREATE TABLE case_documents (id BIGINT PRIMARY KEY, case_reference_number BIGINT "
            + "REFERENCES ccd.case_data(reference) ON DELETE CASCADE)");
        sql("CREATE TABLE case_correspondences (id BIGINT PRIMARY KEY, case_reference_number BIGINT "
            + "REFERENCES ccd.case_data(reference) ON DELETE CASCADE)");
        sql("CREATE TABLE correspondence_document (document_id BIGINT REFERENCES case_documents(id), "
            + "correspondence_id BIGINT REFERENCES case_correspondences(id))");
        sql("INSERT INTO ccd.case_data VALUES (1, " + REFERENCE + ")");
        sql("INSERT INTO ccd.case_event VALUES (1)");
        sql("INSERT INTO ccd.es_queue VALUES (" + REFERENCE + ")");
        sql("INSERT INTO case_documents VALUES (1, " + REFERENCE + ")");
        sql("INSERT INTO case_correspondences VALUES (1, " + REFERENCE + ")");
        sql("INSERT INTO correspondence_document VALUES (1, 1)");
        manager.addReference(REFERENCE);
        manager.addReference(REFERENCE);
    }

    @AfterEach
    void close() throws SQLException {
        // Also clear the retry registry when exercising a failed teardown.
        sql("DROP ALL OBJECTS");
        when(projections.isEnabled()).thenReturn(false);
        sql("CREATE SCHEMA ccd");
        sql("CREATE TABLE ccd.case_data (id BIGINT, reference BIGINT)");
        sql("CREATE TABLE ccd.case_event (case_data_id BIGINT)");
        sql("CREATE TABLE case_documents (id BIGINT, case_reference_number BIGINT)");
        sql("CREATE TABLE case_correspondences (id BIGINT, case_reference_number BIGINT)");
        sql("CREATE TABLE correspondence_document (correspondence_id BIGINT)");
        manager.clearDown(REFERENCE);
        FunctionalTestDataManager.connection.close();
    }

    @Test
    void cleansAssociationsBeforeParentsAndRemovesSuccessfulReference() throws SQLException {
        assertEquals(1, manager.getTestReferences().size());
        when(projections.isEnabled()).thenReturn(true);
        manager.clearDown(REFERENCE);
        assertEquals(0, count("ccd.case_data"));
        assertEquals(0, count("ccd.case_event"));
        assertEquals(0, count("ccd.es_queue"));
        assertEquals(0, count("correspondence_document"));
        verify(projections).deleteCcdPointer(REFERENCE);
        verify(projections).deleteSearchProjections(REFERENCE);
        assertFalse(manager.getTestReferences().contains(REFERENCE));
    }

    @Test
    void rollsBackLocalChildrenAndRetainsReferenceWhenParentDeletionFails() throws SQLException {
        sql("CREATE TABLE blocker (case_id BIGINT REFERENCES ccd.case_data(id))");
        sql("INSERT INTO blocker VALUES (1)");
        assertThrows(RuntimeException.class, () -> manager.clearDown(REFERENCE));
        assertEquals(1, count("ccd.case_data"));
        assertEquals(1, count("ccd.case_event"));
        assertEquals(1, count("case_documents"));
        assertEquals(1, count("correspondence_document"));
        assertTrue(manager.getTestReferences().contains(REFERENCE));
        assertTrue(FunctionalTestDataManager.connection.getAutoCommit());
        verify(projections).isEnabled();
        verifyNoMoreInteractions(projections);
    }

    @Test
    void retainsReferenceForRetryAfterSearchFailure() throws SQLException {
        when(projections.isEnabled()).thenReturn(true);
        doThrow(new IllegalStateException("ES unavailable")).when(projections).deleteSearchProjections(REFERENCE);
        assertThrows(IllegalStateException.class, () -> manager.clearDown(REFERENCE));
        assertEquals(0, count("ccd.case_data"));
        assertTrue(manager.getTestReferences().contains(REFERENCE));
    }

    private void sql(String sql) throws SQLException {
        try (var statement = FunctionalTestDataManager.connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private int count(String table) throws SQLException {
        try (var statement = FunctionalTestDataManager.connection.createStatement();
                var result = statement.executeQuery("SELECT count(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }
}
