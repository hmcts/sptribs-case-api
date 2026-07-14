package uk.gov.hmcts.sptribs.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@TestComponent
public class CaseDocumentManager implements IntegrationTestDataManager {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Long addCaseDocument(Long reference, String binaryUrl, long caseDocumentTypeId, OffsetDateTime timestamp) {
        return jdbcTemplate.queryForObject("""
        INSERT INTO case_documents (
            case_reference_number,
            saved_at,
            document_url,
            document_binary_url,
            document_filename,
            document_type_name,
            case_document_type_id
        )
        VALUES (
            :reference,
            :timestamp,
            'test/document/123',
            :binaryUrl,
            'test-document.pdf',
            'Test Document Type',
            :caseDocumentTypeId
        )
        RETURNING id
        """,
            Map.of(
                "reference", reference,
                "binaryUrl", binaryUrl,
                "caseDocumentTypeId", caseDocumentTypeId,
                "timestamp", timestamp
            ),
            Long.class
        );
    }

    public DocumentEntity findByBinaryUrl(String binaryUrl) {
        return jdbcTemplate.queryForObject("""
            SELECT *
            FROM case_documents
            WHERE document_binary_url = :binaryUrl
            """,
            Map.of("binaryUrl", binaryUrl),
            (rs, rowNum) -> DocumentEntity.builder()
                .id(rs.getLong("id"))
                .caseReferenceNumber(rs.getLong("case_reference_number"))
                .savedAt(
                    rs.getTimestamp("saved_at").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                )
                .documentUrl(rs.getString("document_url"))
                .documentBinaryUrl(rs.getString("document_binary_url"))
                .documentFilename(rs.getString("document_filename"))
                .documentTypeName(rs.getString("document_type_name"))
                .caseDocumentTypeId(rs.getLong("case_document_type_id"))
                .updatedAt(
                    Optional.ofNullable(rs.getTimestamp("updated_at"))
                        .map(Timestamp::toInstant)
                        .map(instant -> instant.atZone(ZoneId.systemDefault()).toOffsetDateTime())
                        .orElse(null)
                )
                .build()
        );
    }

    public int getCount(String binaryUrl) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_documents WHERE document_binary_url = :binaryUrl",
            Map.of("binaryUrl", binaryUrl),
            Integer.class
        );
    }

    @Override
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM case_documents", Map.of());
    }
}
