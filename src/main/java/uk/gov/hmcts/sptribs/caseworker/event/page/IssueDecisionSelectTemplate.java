package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class IssueDecisionSelectTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameSelectTemplate = "issueDecisionSelectTemplate";
        String pageNameAddDocumentFooter = "issueDecisionAddDocumentFooter";
        String pageNamePreviewTemplate = "issueDecisionPreviewTemplate";
        String pageNameUpload = "issueDecisionUploadNotice";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameAddDocumentFooter, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNamePreviewTemplate, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueDecisionDecisionNotice = \"Upload from your computer\"");

        pageBuilder.page(pageNameSelectTemplate)
            .pageLabel("Select a template")
            .pageShowConditions(map)
            .complex(CaseData::getCaseIssueDecision)
            .mandatory(CaseIssueDecision::getIssueDecisionTemplate)
            .done();
    }
}
