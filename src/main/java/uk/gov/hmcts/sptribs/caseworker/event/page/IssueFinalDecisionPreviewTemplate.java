package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class IssueFinalDecisionPreviewTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameSelectTemplate = "issueFinalDecisionSelectTemplate";
        String pageNamePreviewTemplate = "issueFinalDecisionPreviewTemplate";
        String pageNameUpload = "issueFinalDecisionUpload";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueFinalDecisionFinalDecisionNotice = \"Create from a template\"");
        map.put(pageNamePreviewTemplate, "caseIssueFinalDecisionFinalDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueFinalDecisionFinalDecisionNotice = \"Upload from your computer\"");
        pageBuilder.page(pageNamePreviewTemplate)
            .pageLabel("Final decision notice preview")
            .pageShowConditions(map)
            .complex(CaseData::getCaseIssueFinalDecision)
            .readonly(CaseIssueFinalDecision::getFinalDecisionDraft)
            .label("issueFinalDecisionPreviewTemplateInfo","If there are no changes to make, continue to the next screen.")
            .done();
    }
}
