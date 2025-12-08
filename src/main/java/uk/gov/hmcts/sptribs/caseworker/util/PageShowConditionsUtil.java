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

    private static final String ORDER_TYPE_DRAFT_CONDITION = "cicCaseOrderIssuingType = \"DraftOrder\"";
    private static final String ORDER_TYPE_UPLOAD_CONDITION = "cicCaseOrderIssuingType = \"UploadOrder\"";

    private static final String ORDER_EVENT_CREATE_AND_SEND_NEW = "currentEvent = \"create-and-send-order\""
            + " AND cicCaseCreateAndSendIssuingTypes = \"NewOrder\"";
    private static final String ORDER_EVENT_CREATE_AND_SEND_UPLOAD = "currentEvent = \"create-and-send-order\" "
            + "AND cicCaseCreateAndSendIssuingTypes = \"UploadOrder\"";
    private static final String ORDER_EVENT_CREATE_DRAFT = "currentEvent = \"create-draft-order\"";
    private static final String ORDER_EVENT_EDIT_DRAFT = "currentEvent = \"caseworker-edit-draft-order\"";
    private static final String ORDER_EVENT_SEND_ORDER = "currentEvent = \"caseworker-send-order\"";

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

    public static Map<String, String> createAndSendOrderConditions() {
        Map<String, String> map = new HashMap<>();
        map.put("createDraftOrder", ORDER_EVENT_CREATE_DRAFT + " OR (" + ORDER_EVENT_CREATE_AND_SEND_NEW + ")");
        map.put("mainContent", ORDER_EVENT_CREATE_DRAFT + " OR " + ORDER_EVENT_EDIT_DRAFT
            + " OR (" + ORDER_EVENT_CREATE_AND_SEND_NEW + ")");
        map.put("draftOrderDocumentFooter", ORDER_EVENT_CREATE_AND_SEND_NEW);
        map.put("caseworkerSendOrderSelectDraftOrder", "(" + ORDER_EVENT_SEND_ORDER + " AND " + ORDER_TYPE_DRAFT_CONDITION
            + ") OR (" + ORDER_EVENT_CREATE_AND_SEND_NEW + ")");
        map.put("caseworkerSendOrderUploadOrder", "(" + ORDER_EVENT_SEND_ORDER + " AND " + ORDER_TYPE_UPLOAD_CONDITION
            + ") OR (" + ORDER_EVENT_CREATE_AND_SEND_UPLOAD + ")");
        map.put("caseworkerApplyAnonymity", "cicCaseAnonymiseYesOrNo !=\"YES\"");

        return map;
    }

    public static Map<String, String> applyAnonymityShowCondition() {
        Map<String, String> map = new HashMap<>();
        return map;
    }
}
