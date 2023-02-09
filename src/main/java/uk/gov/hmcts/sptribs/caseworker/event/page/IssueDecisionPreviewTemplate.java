package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;

public class IssueDecisionPreviewTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionPreviewTemplate")
            .pageLabel("Decision notice preview")
            .pageShowConditions(issueDecisionShowConditions())
            .complex(CaseData::getCaseIssueDecision)
            .readonly(CaseIssueDecision::getIssueDecisionDraft)
            .label("issueDecisionPreviewTemplateInfo","If there are no changes to make, continue to the next screen.")
            .done();
    }
}
