package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueFinalDecisionNotice implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectIssueNoticeOption")
            .label("selectIssueNoticeOption", "<h1>Create a final decision notice</h1>")
            .complex(CaseData::getCaseIssueFinalDecision)
            .mandatory(CaseIssueFinalDecision::getDecisionNotice)
            .done();
    }
}
