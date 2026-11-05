package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class FirstHearingDateMigrationRepositoryIT extends IntegrationTestBase {

    @Autowired
    private FirstHearingDateMigrationRepository repository;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Test
    void shouldFindAndCountCasesContainingTheRetiredKey() {
        caseDataITManager.addCaseData(123L, "Submitted", """
            {"firstHearingDate": "05 Feb 2024", "hearingDate": "2024-02-05"}
            """);

        assertThat(repository.hasFirstHearingDate(123L)).isTrue();
        assertThat(repository.findNextBatch(0, 10)).containsExactly(123L);
        assertThat(repository.findNextBatch(123L, 10)).isEmpty();
        assertThat(repository.countRemaining()).isEqualTo(1L);
    }

    @Test
    void shouldNotSelectCasesWithoutTheRetiredKey() {
        caseDataITManager.addCaseData(123L, "Submitted", """
            {"hearingDate": "2024-02-05"}
            """);

        assertThat(repository.hasFirstHearingDate(123L)).isFalse();
        assertThat(repository.findNextBatch(0, 10)).isEmpty();
        assertThat(repository.countRemaining()).isZero();
    }
}
