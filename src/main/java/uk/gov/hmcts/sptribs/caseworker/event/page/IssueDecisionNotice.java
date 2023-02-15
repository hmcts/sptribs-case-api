package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueDecisionNotice implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("SelectIssueNoticeOption")
            .pageLabel("Create a decision notice")
            .complex(CaseData::getCaseIssueDecision)
            .mandatory(CaseIssueDecision::getDecisionNotice)
            .done();
    }
}
