package uk.gov.hmcts.sptribs.caseworker.util;

import java.util.HashMap;
import java.util.Map;

public final class PageShowConditionsUtil {

    private PageShowConditionsUtil() {
    }

    public static Map<String, String> issueDecisionShowConditions() {
        String pageNameSelectTemplate = "issueDecisionSelectTemplate";
        String pageNameAddDocumentFooter = "issueDecisionAddDocumentFooter";
        String pageNamePreviewTemplate = "issueDecisionPreviewTemplate";
        String pageNameUpload = "issueDecisionUploadNotice";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameAddDocumentFooter, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNamePreviewTemplate, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueDecisionDecisionNotice = \"Upload from your computer\"");
        return map;
    }
}
