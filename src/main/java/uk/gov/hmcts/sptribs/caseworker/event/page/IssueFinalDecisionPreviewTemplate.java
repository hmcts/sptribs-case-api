package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueFinalDecisionShowConditions;

public class IssueFinalDecisionPreviewTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueFinalDecisionPreviewTemplate")
            .pageLabel("Final decision notice preview")
            .pageShowConditions(issueFinalDecisionShowConditions())
            .complex(CaseData::getCaseIssueFinalDecision)
            .readonlyWithLabel(CaseIssueFinalDecision::getFinalDecisionDraft,"Final decision notice preview")
            .label("issueFinalDecisionPreviewTemplateInfo", "If there are no changes to make, continue to the next screen.")
            .done();
    }
}
