package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueFinalDecisionPreviewTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNamePreviewTemplate = "issueFinalDecisionPreviewTemplate";
        pageBuilder.page(pageNamePreviewTemplate)
            .pageLabel("Final decision notice preview")
            .complex(CaseData::getCaseIssueFinalDecision)
            .readonly(CaseIssueFinalDecision::getFinalDecisionDraft)
            .label("issueFinalDecisionPreviewTemplateInfo","If there are no changes to make, continue to the next screen.")
            .done();
    }
}
