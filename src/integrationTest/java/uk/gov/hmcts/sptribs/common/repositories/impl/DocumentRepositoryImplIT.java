package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.CaseDataManager;
import uk.gov.hmcts.sptribs.CaseDocumentManager;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;

import java.util.Map;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

class DocumentRepositoryImplIT extends IntegrationTestBase {

    @Autowired
    private CaseDataManager caseDataManager;

    @Autowired
    private CaseDocumentManager caseDocumentManager;

    @Autowired
    private DocumentsRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    String binaryUrl = "test/binary/123";

    @BeforeEach
    void setUp() {
        caseDataManager.addCaseData(1111222233334444L, "{}");
        caseDocumentManager.addCaseDocument(1111222233334444L, binaryUrl);
    }

    @Transactional
    @Test
    void givenBinaryURL_thenShouldDeleteEntryFromTable() {

        assertThat(getCount(binaryUrl)).isEqualTo(1);

        repository.deleteEntryByBinaryURL(binaryUrl);

        assertThat(getCount(binaryUrl)).isZero();
    }

    @AfterEach
    void cleardown() {
        caseDataManager.cleanup();
        caseDocumentManager.cleanup();
    }

    private int getCount(String binaryUrl) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_documents WHERE document_binary_url = :binaryUrl",
            Map.of("binaryUrl", binaryUrl),
            Integer.class
        );
    }
}
