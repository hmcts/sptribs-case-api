package uk.gov.hmcts.sptribs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

@TestComponent
public class CaseDocumentManager implements IntegrationTestDataManager {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void addCaseDocument(Long reference, String binaryUrl) {
        jdbcTemplate.update("""
                INSERT INTO case_documents (case_reference_number,
         document_url, document_binary_url, document_filename,
         document_type_name, case_document_type_id)
                VALUES (:reference,'test/document/123',
         :binaryUrl, 'test-document.pdf', 'Test Document Type',
         1)
             """, Map.of(
            "reference", reference,
            "binaryUrl", binaryUrl
        ));
    }

    @Override
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM case_documents", Map.of());
    }
}
