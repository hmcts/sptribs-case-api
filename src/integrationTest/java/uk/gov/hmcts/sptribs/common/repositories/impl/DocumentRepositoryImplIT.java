package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentRepositoryImplIT extends IntegrationTestBase {

    @Autowired
    private DocumentsRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void givenBinaryURL_thenShouldDeleteEntryFromTable() {
        String binaryUrl = "test/binary/123";

        insertDocument(binaryUrl);

        assertThat(getCount(binaryUrl)).isEqualTo(1);

        repository.deleteEntryByBinaryURL(binaryUrl);

        assertThat(getCount(binaryUrl)).isZero();
    }

    private int getCount(String binaryUrl) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_documents WHERE document_binary_url = :binaryUrl",
            Map.of("binaryUrl", binaryUrl),
            Integer.class
        );
    }


    private void insertDocument(String binaryUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("caseReferenceNumber", 123);
        params.put("documentUrl", "http://dm-store/123");
        params.put("documentBinaryUrl", binaryUrl);
        params.put("documentFilename", "test-document.pdf");
        params.put("categoryId", null);
        params.put("documentTypeId", 1);
        params.put("isDraft", false);
        params.put("sentToApplicantViaContactParties", false);

        jdbcTemplate.update("""
            INSERT INTO case_documents (case_reference_number, document_url, document_binary_url,
                document_filename, category_id, document_type_id,
                is_draft, sent_to_applicant_via_contact_parties)
            VALUES (:caseReferenceNumber, :documentUrl, :documentBinaryUrl,
                :documentFilename, :categoryId, :documentTypeId,
                :isDraft, :sentToApplicantViaContactParties)
            """, params);
    }
}
