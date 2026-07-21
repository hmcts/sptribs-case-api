package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@Transactional
class CaseDataRepositoryImplIT extends IntegrationTestBase {

    @Autowired
    private CaseDataRepository repository;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Test
    void givenCCDReference_thenShouldReturnTrueWhenCaseExistsAndValidState() {
        caseDataITManager.addCaseData(123L, "Submitted", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isTrue();
    }

    @Test
    void givenCCDReference_thenShouldReturnFalseWhenStateIsInvalid() {
        caseDataITManager.addCaseData(123L,"Draft", "{}");

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isFalse();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseByEmail() {
        caseDataITManager.addCaseData(123L,"Submitted", """
                {
                  "cicCaseEmail": "test@example.com"
                }
            """);

        var result = repository.findCase("123", "test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseBySearchPartiesEmail() {
        caseDataITManager.addCaseData(123L,"Submitted", """
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
        caseDataITManager.addCaseData(123L,"Submitted", "{}");

        var result = repository.findCase("123", "wrong@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseByEmailAndPostcode() {
        caseDataManager.addCaseData(123L, "Submitted", """
                {
                  "cicCaseEmail": "test@example.com",
                  "cicCaseAddress": {
                    "PostCode": "SW11 1PD"
                  }
                }
            """);

        var result = repository.findCase("123", "test@example.com", "SW11 1PD");

        assertThat(result).isPresent();
    }

    @Test
    void givenCCDReference_thenShouldFindCaseBySearchPartiesEmailAndPostcode() {
        caseDataManager.addCaseData(123L, "Submitted", """
                {
                  "SearchCriteria": {
                    "SearchParties": [
                      {
                        "value": {
                          "EmailAddress": "test@example.com"
                        }
                      }
                    ]
                  },
                  "cicCaseAddress": {
                    "PostCode": "SW11 1PD"
                  }
                }
            """);

        var result = repository.findCase("123", "test@example.com", "SW11 1PD");

        assertThat(result).isPresent();
    }

    @Test
    void givenCCDReference_thenShouldReturnEmptyWhenPostcodeDoesNotMatch() {
        caseDataManager.addCaseData(123L, "Submitted", """
                {
                  "cicCaseEmail": "test@example.com",
                  "cicCaseAddress": {
                    "PostCode": "SW11 1PD"
                  }
                }
            """);

        var result = repository.findCase("123", "test@example.com", "WRONG_POSTCODE");

        assertThat(result).isEmpty();
    }
}
