package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueFinalDecisionNotice implements CcdPageConfiguration {

    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {

        pageBuilder.page("selectIssueNoticeOption")
            .pageLabel("Create a final decision notice")
            .complex(CaseData::getCaseIssueFinalDecision)
            .mandatory(CaseIssueFinalDecision::getFinalDecisionNotice)
            .label("selectIssueNoticeOptionMessage", """
               The decision annex is automatically included via the
               notification  – you do not need to attach it.


               """)
            .done();
    }
}
