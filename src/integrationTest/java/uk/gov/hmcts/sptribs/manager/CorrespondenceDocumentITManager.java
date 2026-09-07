package uk.gov.hmcts.sptribs.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;
import java.util.UUID;

@TestComponent
public class CorrespondenceDocumentITManager implements IntegrationTestDataManager {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM correspondence_document", Map.of());
    }

    public void linkDocument(UUID correspondenceId, Long documentId) {
        jdbcTemplate.update("""
                INSERT INTO correspondence_document (
                    correspondence_id,
                    document_id
                )
                VALUES (
                    :correspondenceId,
                    :documentId
                )
                """,
            Map.of(
                "correspondenceId", correspondenceId,
                "documentId", documentId
            )
        );
    }
}
