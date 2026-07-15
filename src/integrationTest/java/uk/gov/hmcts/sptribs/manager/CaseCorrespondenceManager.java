package uk.gov.hmcts.sptribs.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@TestComponent
public class CaseCorrespondenceManager implements IntegrationTestDataManager {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM case_correspondences", Map.of());
    }

    public void addCorrespondence(
        UUID correspondenceId,
        long caseReference,
        Party party,
        OffsetDateTime sentOn
    ) {
        jdbcTemplate.update("""
        INSERT INTO case_correspondences (
            case_reference_number,
            id,
            event_type,
            sent_on,
            sent_from,
            sent_to,
            document_url,
            document_binary_url,
            document_filename,
            correspondence_type,
            receiving_party
        )
        VALUES (
            :caseReference,
            :correspondenceId,
            'TEST_EVENT',
            :sentOn,
            'tribunal@test.com',
            'recipient@test.com',
            'test/document/123',
            'test/document/123/binary',
            'test-document.pdf',
            'EMAIL',
            CAST(:receivingParty AS party)
        )
        """,
            Map.of(
                "caseReference", caseReference,
                "correspondenceId", correspondenceId,
                "sentOn", Timestamp.from(sentOn.toInstant()),
                "receivingParty", party.name()
            )
        );
    }
}
