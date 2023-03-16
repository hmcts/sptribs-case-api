package uk.gov.hmcts.sptribs.caseworker.util;

import java.util.HashMap;
import java.util.Map;

public final class PageShowConditionsUtil {
    private static final String ISSUE_DECISION_TEMPLATE_CONDITION = "caseIssueDecisionDecisionNotice = \"Create from a template\"";
    private static final String ISSUE_DECISION_UPLOAD_CONDITION = "caseIssueDecisionDecisionNotice = \"Upload from your computer\"";
    private static final String ISSUE_FINAL_DECISION_TEMPLATE_CONDITION =
        "caseIssueFinalDecisionFinalDecisionNotice = \"Create from a template\"";
    private static final String ISSUE_FINAL_DECISION_UPLOAD_CONDITION =
        "caseIssueFinalDecisionFinalDecisionNotice = \"Upload from your computer\"";

    private static final String EDIT_SUMMARY_SHOW_WARNING_PAGE = " hearingSummaryExists != \"YES\"";
    private static final String EDIT_SUMMARY = "currentEvent != \"edit-hearing-summary\" OR  hearingSummaryExists = \"YES\"";

    private PageShowConditionsUtil() {
    }

    public static Map<String, String> issueDecisionShowConditions() {
        Map<String, String> map = new HashMap<>();
        map.put("issueDecisionSelectTemplate", ISSUE_DECISION_TEMPLATE_CONDITION);
        map.put("issueDecisionAddDocumentFooter", ISSUE_DECISION_TEMPLATE_CONDITION);
        map.put("issueDecisionMainContent", ISSUE_DECISION_TEMPLATE_CONDITION);
        map.put("issueDecisionPreviewTemplate", ISSUE_DECISION_TEMPLATE_CONDITION);
        map.put("issueDecisionUploadNotice", ISSUE_DECISION_UPLOAD_CONDITION);
        return map;
    }

    public static Map<String, String> issueFinalDecisionShowConditions() {
        Map<String, String> map = new HashMap<>();
        map.put("issueFinalDecisionSelectTemplate", ISSUE_FINAL_DECISION_TEMPLATE_CONDITION);
        map.put("issueFinalDecisionAddDocumentFooter", ISSUE_FINAL_DECISION_TEMPLATE_CONDITION);
        map.put("issueFinalDecisionMainContent", ISSUE_FINAL_DECISION_TEMPLATE_CONDITION);
        map.put("issueFinalDecisionPreviewTemplate", ISSUE_FINAL_DECISION_TEMPLATE_CONDITION);
        map.put("issueFinalDecisionUpload", ISSUE_FINAL_DECISION_UPLOAD_CONDITION);
        return map;
    }

    public static Map<String, String> editSummaryShowConditions() {
        Map<String, String> map = new HashMap<>();
        map.put("hearingTypeAndFormat", EDIT_SUMMARY);
        map.put("hearingAttendeesRole", EDIT_SUMMARY);
        map.put("hearingAttendees", EDIT_SUMMARY);
        map.put("listingDetails", EDIT_SUMMARY);
        map.put("hearingOutcome", EDIT_SUMMARY);
        map.put("hearingRecordingUpload", EDIT_SUMMARY);

        map.put("editHearingSummary", EDIT_SUMMARY_SHOW_WARNING_PAGE);
        return map;
    }
}
