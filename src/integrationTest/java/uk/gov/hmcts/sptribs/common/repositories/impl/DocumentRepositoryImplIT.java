package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;

import java.util.Map;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@Sql(scripts = "/sql/data/case_documents_data/case_documents_data.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/data/case_documents_data/clearup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DocumentRepositoryImplIT extends IntegrationTestBase {

    @Autowired
    private DocumentsRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    @Test
    void givenBinaryURL_thenShouldDeleteEntryFromTable() {
        String binaryUrl = "test/binary/123";

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
}
