package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class IssueDecisionSelectTemplate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameSelectTemplate = "SelectTemplate";
        String pageNameUpload = "UploadNotice";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueDecisionDecisionNotice = \"Upload from your computer\"");
    }
}
