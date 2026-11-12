package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;
import uk.gov.hmcts.sptribs.systemupdate.service.FirstHearingDateMigrationRepository;
import uk.gov.hmcts.sptribs.systemupdate.service.ScheduledTaskRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@TestPropertySource(properties = {
    "ccd.decentralised-runtime.system-user.id=00000000-0000-0000-0000-000000000000",
    "ccd.decentralised-runtime.system-user.username=case-service-system",
    "ccd.decentralised-runtime.system-user.first-name=Case Service",
    "ccd.decentralised-runtime.system-user.last-name=System",
    "feature.remove-first-hearing-date-task.enabled=true",
    "feature.remove-first-hearing-date-task.batchSize=2",
    "feature.remove-first-hearing-date-task.batchPauseMs=0"
})
class SystemRemoveFirstHearingDateTaskIT extends IntegrationTestBase {

    private static final String TASK_NAME = "SystemRemoveFirstHearingDateTask";
    private static final long SELECTED_REFERENCE = 123L;
    private static final long NULL_DATE_REFERENCE = 124L;
    private static final long OTHER_REFERENCE = 125L;
    private static final long UNAFFECTED_REFERENCE = 126L;

    @Autowired
    private SystemRemoveFirstHearingDateTask task;

    @Autowired
    private ScheduledTaskRunner taskRunner;

    @Autowired
    private FirstHearingDateMigrationRepository repository;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldMigrateSelectedThenRemainingCasesWithoutDuplicateEvents() throws Exception {
        caseDataITManager.addCaseData(SELECTED_REFERENCE, "Submitted", """
            {"firstHearingDate": "05 Feb 2024", "hearingDate": "2024-02-05"}
            """);
        seedCase(2L, NULL_DATE_REFERENCE, """
            {"firstHearingDate": null, "hearingDate": "2024-02-06"}
            """);
        seedCase(3L, OTHER_REFERENCE, """
            {"firstHearingDate": "07 Feb 2024", "hearingDate": "2024-02-07"}
            """);
        seedCase(4L, UNAFFECTED_REFERENCE, """
            {"hearingDate": "2024-02-08"}
            """);

        final JsonNode unaffectedData = caseData(UNAFFECTED_REFERENCE);
        final int originalVersion = caseVersion(SELECTED_REFERENCE);
        assertThat(repository.countRemaining()).isEqualTo(3);

        ReflectionTestUtils.setField(task, "caseReference", String.valueOf(SELECTED_REFERENCE));
        try {
            taskRunner.run(TASK_NAME);
            taskRunner.run(TASK_NAME);
        } finally {
            ReflectionTestUtils.setField(task, "caseReference", "");
        }

        assertThat(repository.hasFirstHearingDate(SELECTED_REFERENCE)).isFalse();
        assertThat(caseData(SELECTED_REFERENCE)).isEqualTo(objectMapper.readTree("""
            {"hearingDate": "2024-02-05"}
            """));
        assertThat(caseState(SELECTED_REFERENCE)).isEqualTo("Submitted");
        assertThat(caseVersion(SELECTED_REFERENCE)).isEqualTo(originalVersion);
        assertThat(eventCount(SELECTED_REFERENCE)).isEqualTo(1);
        assertThat(repository.countRemaining()).isEqualTo(2);
        assertThat(repository.hasFirstHearingDate(NULL_DATE_REFERENCE)).isTrue();
        assertThat(repository.hasFirstHearingDate(OTHER_REFERENCE)).isTrue();
        assertThat(caseData(UNAFFECTED_REFERENCE)).isEqualTo(unaffectedData);
        assertThat(eventCount(UNAFFECTED_REFERENCE)).isZero();

        taskRunner.run(TASK_NAME);
        taskRunner.run(TASK_NAME);

        assertThat(repository.countRemaining()).isZero();
        assertThat(caseData(NULL_DATE_REFERENCE)).isEqualTo(objectMapper.readTree("""
            {"hearingDate": "2024-02-06"}
            """));
        assertThat(caseData(OTHER_REFERENCE)).isEqualTo(objectMapper.readTree("""
            {"hearingDate": "2024-02-07"}
            """));
        assertThat(caseData(UNAFFECTED_REFERENCE)).isEqualTo(unaffectedData);
        for (long reference : new long[]{SELECTED_REFERENCE, NULL_DATE_REFERENCE, OTHER_REFERENCE}) {
            assertThat(caseState(reference)).isEqualTo("Submitted");
            assertThat(caseVersion(reference)).isEqualTo(originalVersion);
            assertThat(eventCount(reference)).isEqualTo(1);
        }
        assertThat(eventCount(UNAFFECTED_REFERENCE)).isZero();
    }

    private void seedCase(long id, long reference, String data) {
        jdbcTemplate.update("""
            INSERT INTO ccd.case_data (id, reference, jurisdiction, case_type_id, state, data, security_classification)
            VALUES (:id, :reference, 'ST_CIC', 'CriminalInjuriesCompensation', 'Submitted', CAST(:data AS jsonb),
                CAST('PUBLIC' AS ccd.securityclassification))
            """, Map.of("id", id, "reference", reference, "data", data));
    }

    private JsonNode caseData(long reference) throws Exception {
        String data = jdbcTemplate.queryForObject(
            "SELECT c.data::text FROM ccd.case_data c WHERE c.reference = :reference",
            Map.of("reference", reference), String.class);
        return objectMapper.readTree(data);
    }

    private String caseState(long reference) {
        return jdbcTemplate.queryForObject(
            "SELECT c.state FROM ccd.case_data c WHERE c.reference = :reference",
            Map.of("reference", reference), String.class);
    }

    private int caseVersion(long reference) {
        return jdbcTemplate.queryForObject(
            "SELECT c.version FROM ccd.case_data c WHERE c.reference = :reference",
            Map.of("reference", reference), Integer.class);
    }

    private long eventCount(long reference) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ccd.case_event e JOIN ccd.case_data c ON e.case_data_id = c.id "
                + "WHERE c.reference = :reference AND e.event_id = :eventId",
            Map.of("reference", reference, "eventId", SYSTEM_MIGRATE_CASE), Long.class);
    }
}
