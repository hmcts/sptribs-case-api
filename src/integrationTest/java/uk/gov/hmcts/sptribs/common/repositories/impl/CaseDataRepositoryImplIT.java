package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.manager.CaseDataManager;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@Transactional
class CaseDataRepositoryImplIT extends IntegrationTestBase {

    @Autowired
    private CaseDataRepository repository;

    @Autowired
    private CaseDataManager caseDataManager;

    @Test
    void givenCCDReference_thenShouldReturnTrueWhenCaseExistsAndValidState() {
        caseDataManager.addCaseData(123L, "Submitted", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isTrue();
    }

    @Test
    void givenCCDReference_thenShouldReturnFalseWhenStateIsInvalid() {
        caseDataManager.addCaseData(123L,"Draft", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isFalse();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseByEmail() {
        caseDataManager.addCaseData(123L,"Submitted", """
                {
                  "cicCaseEmail": "test@example.com"
                }
            """);

        var result = repository.findCase("123", "test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseBySearchPartiesEmail() {
        caseDataManager.addCaseData(123L,"Submitted", """
                {
                  "SearchCriteria": {
                    "SearchParties": [
                      {
                        "value": {
                          "EmailAddress": "test@example.com"
                        }
                      }
                    ]
                  }
                }
            """);

        var result = repository.findCase("123", "test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void givenCCDReference_thenShouldReturnEmptyWhenEmailDoesNotMatch() {
        caseDataManager.addCaseData(123L,"Submitted", "{}");

        var result = repository.findCase("123", "wrong@example.com");

        assertThat(result).isEmpty();
    }
}
