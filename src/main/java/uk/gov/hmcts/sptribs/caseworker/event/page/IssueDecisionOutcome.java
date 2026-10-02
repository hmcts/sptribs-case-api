package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueDecisionOutcome implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionOutcome")
            .pageLabel("Decision outcome")
            .complex(CaseData::getCaseIssueDecision)
            .mandatoryWithLabel(CaseIssueDecision::getDecisionOutcome, "What is the decision outcome?")
            .done();
    }
}
