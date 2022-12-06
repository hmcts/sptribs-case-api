package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueFinalDecisionNotice implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectIssueNoticeOption")
            .pageLabel("Create a final decision notice")
            .complex(CaseData::getCaseIssueFinalDecision)
            .mandatory(CaseIssueFinalDecision::getDecisionNotice)
            .label("selectIssueNoticeOptionMessage", """
                The decision annex is automatically included via the
                notification  – you do not need to attach it


                """)
            .done();
    }
}
