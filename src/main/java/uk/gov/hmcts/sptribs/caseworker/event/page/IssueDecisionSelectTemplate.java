package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;

public class IssueDecisionSelectTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionSelectTemplate", this::midEvent)
            .pageLabel("Select a template")
            .pageShowConditions(issueDecisionShowConditions())
            .complex(CaseData::getCaseIssueDecision)
            .mandatory(CaseIssueDecision::getIssueDecisionTemplate)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData caseData = details.getData();
        DecisionTemplate decision = caseData.getCaseIssueDecision().getIssueDecisionTemplate();
        caseData.setDecisionMainContent(EventUtil.getMainContent(decision));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

}
