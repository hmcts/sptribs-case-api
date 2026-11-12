package uk.gov.hmcts.sptribs.systemupdate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutor;
import uk.gov.hmcts.ccd.sdk.SystemEventResult;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult.Outcome.EXECUTED;
import static uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult.Outcome.REPLAYED;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@TestPropertySource(properties = {
    "ccd.decentralised-runtime.system-user.id=00000000-0000-0000-0000-000000000000",
    "ccd.decentralised-runtime.system-user.username=case-service-system",
    "ccd.decentralised-runtime.system-user.first-name=Case Service",
    "ccd.decentralised-runtime.system-user.last-name=System"
})
class FirstHearingDateSystemEventIT extends IntegrationTestBase {

    private static final long CASE_REFERENCE = 123L;

    @Autowired
    private FirstHearingDateMigrationRepository repository;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Autowired
    private SystemEventExecutor executor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void shouldRemoveOnlyRetiredFieldAndAuditExactlyOnceOnReplay() throws Exception {
        caseDataITManager.addCaseData(CASE_REFERENCE, "Submitted", """
            {"firstHearingDate": "05 Feb 2024", "hearingDate": "2024-02-05"}
            """);
        int initialVersion = caseVersion();
        UUID key = UUID.randomUUID();

        var first = executor.execute(CASE_REFERENCE, key, context -> {
            assertThat(repository.removeFirstHearingDate(context.caseReference())).isTrue();
            return SystemEventResult.withoutStateTransition(
                SYSTEM_MIGRATE_CASE, "Migrate case data", "Remove retired firstHearingDate field");
        });
        var replay = executor.execute(CASE_REFERENCE, key, context -> {
            throw new AssertionError("The action must not execute on replay");
        });

        assertThat(first.outcome()).isEqualTo(EXECUTED);
        assertThat(replay.outcome()).isEqualTo(REPLAYED);
        assertThat(replay.eventId()).isEqualTo(first.eventId());
        assertThat(repository.hasFirstHearingDate(CASE_REFERENCE)).isFalse();
        assertThat(caseData().get("hearingDate").asText()).isEqualTo("2024-02-05");
        assertThat(caseData().has("firstHearingDate")).isFalse();
        assertThat(eventCount()).isEqualTo(1);
        assertThat(caseVersion()).isEqualTo(initialVersion);
    }

    @Test
    void shouldRollBackCaseChangeWhenEventFails() throws Exception {
        caseDataITManager.addCaseData(CASE_REFERENCE, "Submitted", """
            {"firstHearingDate": null, "hearingDate": "2024-02-05"}
            """);
        int initialVersion = caseVersion();

        assertThatThrownBy(() -> executor.execute(CASE_REFERENCE, UUID.randomUUID(), context -> {
            assertThat(repository.removeFirstHearingDate(context.caseReference())).isTrue();
            throw new IllegalStateException("Event failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(repository.hasFirstHearingDate(CASE_REFERENCE)).isTrue();
        assertThat(caseData().get("hearingDate").asText()).isEqualTo("2024-02-05");
        assertThat(caseData().has("firstHearingDate")).isTrue();
        assertThat(eventCount()).isZero();
        assertThat(caseVersion()).isEqualTo(initialVersion);
    }

    private int caseVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT c.version FROM ccd.case_data c WHERE c.reference = :reference",
            Map.of("reference", CASE_REFERENCE), Integer.class);
    }

    private JsonNode caseData() throws Exception {
        String data = jdbcTemplate.queryForObject(
            "SELECT c.data::text FROM ccd.case_data c WHERE c.reference = :reference",
            Map.of("reference", CASE_REFERENCE), String.class);
        return new ObjectMapper().readTree(data);
    }

    private long eventCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ccd.case_event e JOIN ccd.case_data c ON e.case_data_id = c.id "
                + "WHERE c.reference = :reference AND e.event_id = :eventId",
            Map.of("reference", CASE_REFERENCE, "eventId", SYSTEM_MIGRATE_CASE), Long.class);
    }
}
