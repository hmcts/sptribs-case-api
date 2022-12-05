package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class IssueFinalDecisionSelectTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameSelectTemplate = "issueFinalDecisionSelectTemplate";
        String pageNameUpload = "issueFinalDecisionUpload";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueFinalDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueFinalDecisionDecisionNotice = \"Upload from your computer\"");
        pageBuilder.page(pageNameSelectTemplate)
            .pageLabel("Select a template")
            .pageShowConditions(map)
            .complex(CaseData::getCaseIssueFinalDecision)
            .mandatory(CaseIssueFinalDecision::getIssueFinalDecisionTemplate)
            .done();
    }
}
