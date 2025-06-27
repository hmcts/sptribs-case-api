package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class MigrateRetiredFieldsTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MigrateRetiredFields migrateRetiredFields;

    @Test
    void shouldMigrateFields() {
        final CaseData caseData = TestDataHelper.awaitingOutcomeData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = migrateRetiredFields.apply(caseDetails);

        assertNotNull(result);
        assertEquals(State.AwaitingOutcome, result.getData().getCaseStatus());
        assertEquals(caseDetails.getData(), result.getData());
    }

    @Test
    void shouldMigrateAllKeysAndNullOldFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("cicBundles", "bundleValue");
        data.put("cicCaseFirstDueDate", "01 Jan 2024");

        Map<String, Object> migrated = RetiredFields.migrate(data);

        assertEquals("bundleValue", migrated.get("caseBundles"));
        assertNull(migrated.get("cicBundles"));

        assertNotNull(migrated.get("cicCaseFirstOrderDueDate"));
        assertNull(migrated.get("cicCaseFirstDueDate"));

        assertEquals(
            RetiredFields.getVersion(),
            migrated.get("dataVersion")
        );
    }

    @Test
    void shouldHandleNullAndMissingKeysAndValuesInMigrate() {
        Map<String, Object> dataWithNull = new HashMap<>();
        dataWithNull.put("cicBundles", null);

        Map<String, Object> migratedNull = RetiredFields.migrate(dataWithNull);

        assertNull(migratedNull.get("caseBundles"));
        assertNull(migratedNull.get("cicBundles"));
        assertEquals(
            RetiredFields.getVersion(),
            migratedNull.get("dataVersion")
        );

        Map<String, Object> dataMissing = new HashMap<>();
        Map<String, Object> migratedMissing = RetiredFields.migrate(dataMissing);

        assertNull(migratedMissing.get("caseBundles"));
        assertNull(migratedMissing.get("cicBundles"));
        assertEquals(
            RetiredFields.getVersion(),
            migratedMissing.get("dataVersion")
        );
    }

    @Test
    void shouldHandleInvalidDateFormatInMigrate() {
        Map<String, Object> data = new HashMap<>();
        data.put("cicCaseFirstDueDate", "invalid-date");

        java.io.ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            Map<String, Object> migrated = RetiredFields.migrate(data);

            assertNull(migrated.get("cicCaseFirstDueDate"));
            assertNull(migrated.get("cicCaseFirstOrderDueDate"));
            assertEquals(
                RetiredFields.getVersion(),
                migrated.get("dataVersion")
            );

            String errOutput = errContent.toString();
            assertTrue(errOutput.contains("Could not migrate case"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void shouldHandleEmptyStringForFirstDueDate() {
        Map<String, Object> data = new HashMap<>();
        data.put("cicCaseFirstDueDate", "");

        Map<String, Object> migrated = RetiredFields.migrate(data);

        assertNull(migrated.get("cicCaseFirstOrderDueDate"));
        assertNull(migrated.get("cicCaseFirstDueDate"));
        assertEquals(
            RetiredFields.getVersion(),
            migrated.get("dataVersion")
        );
    }
}
