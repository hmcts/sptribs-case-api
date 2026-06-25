package uk.gov.hmcts.sptribs.common;

public final class CommonConstants {

    public static final String EMPTY_PLACEHOLDER = "";

    public static final String CONTACT_NAME = "ContactName";
    public static final String TRIBUNAL_NAME = "TribunalName";
    public static final String CIC_CASE_NUMBER = "CicCaseNumber";
    public static final String CIC_CASE_SUBJECT_NAME = "CicCaseSubjectFullName";
    public static final String CONTACT_PARTY_INFO = "Contact_party_information";
    public static final String CIC_CASE_APPLICANT_NAME = "CicCaseApplicantFullName";
    public static final String CIC_CASE_REPRESENTATIVE_NAME = "CicCaseRepresentativeFullName";
    public static final String CIC_CASE_RESPONDENT_NAME = "CicCaseRespondentFullName";
    public static final String CIC_CASE_TRIBUNAL_NAME = "CicCaseTribunalFullName";
    public static final String CIC_CASE_HEARING_TYPE = "HearingType";
    public static final String CIC_CASE_HEARING_VENUE = "HearingVenue";
    public static final String CIC_CASE_HEARING_DATE = "HearingDate";
    public static final String CIC_CASE_HEARING_TIME = "HearingTime";
    public static final String CIC_CASE_HEARING_INFO = "HearingInfo";
    public static final String CIC_CASE_RECORD_HEARING_FORMAT_VIDEO = "hearingFormatVideo";
    public static final String CIC_CASE_RECORD_VIDEO_CALL_LINK = "videoCallLink";
    public static final String CIC_CASE_RECORD_FORMAT_TEL = "hearingFormatTelephone";
    public static final String CIC_CASE_RECORD_CONF_CALL_NUM = "conferenceCallNumber";
    public static final String CIC_CASE_RECORD_HEARING_1FACE_TO_FACE = "hearingFormatFaceToFace";
    public static final String CIC_CASE_RECORD_REMOTE_HEARING = "Remote Hearing";
    public static final String CIC_CASE_UK_DATE_FORMAT = "dd-MM-yyyy";
    public static final String CIC_BUNDLE_DUE_DATE_TEXT = "bundleDueDateText";

    public static final String HEARING_DATE = "HearingDate";
    public static final String HEARING_TIME = "HearingTime";
    public static final String REINSTATE_REASON = "ReinstatementReason";
    public static final String TRIBUNAL_ORDER = "TribunalOrder";
    public static final String DECISION_NOTICE = "DecisionNotice";
    public static final String FINAL_DECISION_NOTICE = "FinalDecisionNotice";
    public static final String FINAL_DECISION_GUIDANCE = "FinalDecisionGuidance";
    public static final String CASE_DOCUMENT = "CaseDocument";
    public static final String DOC_AVAILABLE = "DocumentAvailable";
    public static final String STAY_EXPIRATION_DATE = "StayExpirationDate";
    public static final String STAY_REASON = "stayStayReason";
    public static final String STAY_ADDITIONAL_DETAIL = "stayAdditionalDetail";
    public static final String CLOSURE_REASON = "ClosureReason";
    public static final String CLOSURE_INFORMATION = "ClosureInformation";
    public static final String NONE_PROVIDED = "None provided";
    public static final String ADDRESS_LINE_1 = "address_line_1";
    public static final String ADDRESS_LINE_2 = "address_line_2";
    public static final String ADDRESS_LINE_3 = "address_line_3";
    public static final String ADDRESS_LINE_4 = "address_line_4";
    public static final String ADDRESS_LINE_5 = "address_line_5";
    public static final String ADDRESS_LINE_6 = "address_line_6";
    public static final String ADDRESS_LINE_7 = "address_line_7";
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String CATEGORY_ID = "categoryId";
    public static final String SERVICE_ID = "serviceId";
    public static final String MICRO_SERVICE_ID = "sptribs_case_api";
    public static final String CATEGORY_ID_LINK_REASON = "CaseLinkingReasonCode";

    public static final String MARKUP_SEPARATOR = "]";
    public static final String CIC = "Criminal Injuries Compensation Tribunal";

    public static final String STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG = "File Name should contain at least 2 and "
        + "not more than 50 Chars";
    public static final String BUNDLE_DESCRIPTION_FIELD_LENGTH_ERROR_MSG = "Bundle Description should not contain "
        + "more than 255 Chars";

    public static final String DEATH_OF_APPELLANT_EMAIL_CONTENT =
        "Please refer to the Tribunal’s recent directions for further details";

    public static final String TRIBUNAL_NAME_VALUE = "First-tier Tribunal (CIC)";
    public static final String TRIBUNAL_EMAIL_VALUE = "CIC.enquiries@justice.gov.uk";

    public static final String HAS_CICA_NUMBER = "hasCicaNumber";
    public static final String CICA_REF_NUMBER = "cicaRefNumber";

    private CommonConstants() {
    }
}
