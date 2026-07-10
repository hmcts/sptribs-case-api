package uk.gov.hmcts.sptribs.common.repositories.impl;

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
import java.util.UUID;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@Transactional
class DocumentsRepositoryImplIT extends IntegrationTestBase {


    @Autowired
    private DocumentsRepository repository;

    @Autowired
    private CaseDataManager caseDataManager;

    @Autowired
    private CaseDocumentManager caseDocumentManager;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        caseDataManager.addCaseData(123L, "test", "{}");
    }

    @Test
    void givenBinaryURL_thenShouldDeleteEntryFromTable() {

        String binaryUrl = UUID.randomUUID().toString();

        caseDocumentManager.addCaseDocument(123L, binaryUrl);

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
