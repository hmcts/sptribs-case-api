package uk.gov.hmcts.sptribs.caseworker.util;

import java.util.HashMap;
import java.util.Map;

public final class PageShowConditionsUtil {
    private static final String TEMPLATE_CONDITION = "caseIssueDecisionDecisionNotice = \"Create from a template\"";
    private static final String UPLOAD_CONDITION = "caseIssueDecisionDecisionNotice = \"Upload from your computer\"";

    private PageShowConditionsUtil() {
    }

    public static Map<String, String> issueDecisionShowConditions() {
        String pageNameSelectTemplate = "issueDecisionSelectTemplate";
        String pageNameAddDocumentFooter = "issueDecisionAddDocumentFooter";
        String pageNamePreviewTemplate = "issueDecisionPreviewTemplate";
        String pageNameMainContent = "issueDecisionMainContent";
        String pageNameUpload = "issueDecisionUploadNotice";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, TEMPLATE_CONDITION);
        map.put(pageNameAddDocumentFooter, TEMPLATE_CONDITION);
        map.put(pageNameMainContent, TEMPLATE_CONDITION);
        map.put(pageNamePreviewTemplate, TEMPLATE_CONDITION);
        map.put(pageNameUpload, UPLOAD_CONDITION);
        return map;
    }
}
