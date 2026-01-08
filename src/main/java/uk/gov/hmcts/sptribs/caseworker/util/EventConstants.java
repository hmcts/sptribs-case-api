package uk.gov.hmcts.sptribs.caseworker.util;

public final class EventConstants {
    public static final String HYPHEN = "-";
    public static final String DOUBLE_HYPHEN = "--";
    public static final String SPACE = " ";
    public static final String COLON = " :";
    public static final String CURRENT_EVENT = "currentEvent = \"";

    public static final String DRAFT = "DRAFT";
    public static final String SENT = "SENT";
    public static final String COURT_TYPE_ID = "14";

    public static final String CASEWORKER_CREATE_CASE = "caseworker-create-case";
    public static final String CASEWORKER_CANCEL_HEARING = "caseworker-cancel-hearing";
    public static final String CASEWORKER_ADD_NOTE = "caseworker-add-note";
    public static final String CASEWORKER_CASE_BUILT = "caseworker-case-built";
    public static final String CASEWORKER_CASE_FLAG = "createFlags";
    public static final String CASEWORKER_MANAGE_CASE_FLAG = "manageFlags";
    public static final String CASEWORKER_CLOSE_THE_CASE = "caseworker-close-the-case";
    public static final String CASEWORKER_CONTACT_PARTIES = "contact-parties";
    public static final String RESPONDENT_CONTACT_PARTIES = "respondent-contact-parties";
    public static final String CASEWORKER_CREATE_DRAFT_ORDER = "create-draft-order";
    public static final String CASEWORKER_CREATE_HEARING_SUMMARY = "create-hearing-summary";
    public static final String CASEWORKER_EDIT_HEARING_SUMMARY = "edit-hearing-summary";
    public static final String CASEWORKER_EDIT_CASE = "edit-case";
    public static final String CASEWORKER_EDIT_DRAFT_ORDER = "caseworker-edit-draft-order";
    public static final String CASEWORKER_EDIT_CICA_CASE_DETAILS = "caseworker-edit-cica-case-details";
    public static final String CASEWORKER_EDIT_RECORD_LISTING = "caseworker-edit-record-listing";
    public static final String CASEWORKER_ISSUE_CASE = "caseworker-issue-case";
    public static final String CASEWORKER_DOCUMENT_MANAGEMENT = "caseworker-document-management";
    public static final String RESPONDENT_DOCUMENT_MANAGEMENT = "respondent-document-management";
    public static final String CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE = "caseworker-remove-document";
    public static final String CASEWORKER_DOCUMENT_MANAGEMENT_AMEND = "caseworker-amend-document";
    public static final String CASEWORKER_ISSUE_DECISION = "caseworker-issue-decision";
    public static final String CASEWORKER_ISSUE_FINAL_DECISION = "caseworker-issue-final-decision";
    public static final String CASEWORKER_LINK_CASE = "createCaseLink";
    public static final String CASEWORKER_MAINTAIN_LINK_CASE = "maintainCaseLink";
    public static final String CASEWORKER_AMEND_DUE_DATE = "caseworker-amend-due-date";
    public static final String CASEWORKER_POSTPONE_HEARING = "caseworker-postpone-hearing";
    public static final String CASEWORKER_RECORD_LISTING = "caseworker-record-listing";
    public static final String CASEWORKER_REMOVE_STAY = "caseworker-remove-stay";
    public static final String CASEWORKER_SEND_ORDER = "caseworker-send-order";
    public static final String CASEWORKER_STAY_THE_CASE = "caseworker-stay-the-case";
    public static final String CASEWORKER_REINSTATE_CASE = "caseworker-reinstate-state";
    public static final String CASEWORKER_HEARING_OPTIONS = "caseworker-hearing-options";
    public static final String CASEWORKER_CLEAR_HEARING_OPTIONS = "caseworker-clear-hearing-options";
    public static final String CASEWORKER_PANEL_COMPOSITION = "caseworker-panel-composition";
    public static final String CASEWORKER_EDIT_PANEL_COMPOSITION = "caseworker-edit-panel-composition";
    public static final String CASEWORKER_UPDATE_CASE_DATA = "update-case-data";
    public static final String SUPERUSER_REINDEX_CASES = "superuser-reindex-cases";
    public static final String CHANGE_SECURITY_CLASS = "change-security-class";
    public static final String CASEWORKER_REFER_TO_JUDGE = "refer-to-judge";
    public static final String CREATE_BUNDLE = "createBundle";
    public static final String ASYNC_STITCH_COMPLETE = "asyncStitchingComplete";
    public static final String EDIT_BUNDLE = "editBundle";
    public static final String CLONE_BUNDLE = "cloneBundle";

    public static final String CITIZEN_CIC_CREATE_CASE = "citizen-cic-create-dss-application";
    public static final String CITIZEN_CIC_UPDATE_CASE = "citizen-cic-update-dss-application";
    public static final String CITIZEN_CIC_SUBMIT_CASE = "citizen-cic-submit-dss-application";
    public static final String CITIZEN_DSS_UPDATE_CASE_SUBMISSION = "citizen-cic-dss-update-case";

    public static final String CASEWORKER_REFER_TO_LEGAL_OFFICER = "refer-to-legal-officer";
    public static final String YES = "YES";

    private EventConstants() {
    }
}
