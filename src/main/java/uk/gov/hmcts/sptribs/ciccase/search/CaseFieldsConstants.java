package uk.gov.hmcts.sptribs.ciccase.search;

public final class CaseFieldsConstants {
    //Fields
    public static final String SUBJECT_NAME = "cicCaseFullName";
    public static final String SUBJECT_DATE_OF_BIRTH = "cicCaseDateOfBirth";
    public static final String SUBJECT_ADDRESS = "cicCaseAddress";
    public static final String SUBJECT_EMAIL = "cicCaseEmail";
    public static final String SUBJECT_PHONE_NUMBER = "cicCasePhoneNumber";

    public static final String HEARING_DATE = "firstHearingDate";
    public static final String HEARING_LOCATION = "hearingVenueName";
    public static final String HEARING_FORMAT = "hearingFormat";
    public static final String SHORT_NOTICE = "shortNotice";
    public static final String CASE_CATEGORY = "cicCaseCaseCategory";
    public static final String CASE_SUBCATEGORY = "cicCaseCaseSubcategory";
    public static final String CASE_DETAILS = "case-details";
    public static final String CASE_STATE = "[STATE]";
    public static final String CCD_REFERENCE = "[CASE_REFERENCE]";
    public static final String CASE_REGION = "cicCaseRegionCIC";
    public static final String CASE_STATE_LABEL = "LabelState";
    public static final String CASE_RECEIVED_DATE = "cicCaseCaseReceivedDate";
    public static final String CASE_CLAIM_LINKED = "cicCaseClaimLinkedToCic";
    public static final String IS_CASE_IN_TIME = "cicCaseIsCaseInTime";
    public static final String DUE_DATE = "cicCaseFirstOrderDueDate";
    public static final String SCHEME = "cicCaseSchemeCic";
    public static final String LAST_MODIFIED_DATE = "[LAST_MODIFIED_DATE]";
    public static final String LAST_STATE_MODIFIED_DATE = "[LAST_STATE_MODIFIED_DATE]";
    public static final String CASE_CICA_REFERENCE = "cicCaseCicaReferenceNumber";
    public static final String PANEL_COMPOSITION = "panelComposition";

    public static final String APPLICANT_DETAILS = "applicantDetails";
    public static final String APPLICANT_NAME = "cicCaseApplicantFullName";
    public static final String APPLICANT_EMAIL = "cicCaseApplicantEmailAddress";
    public static final String APPLICANT_DATE_OF_BIRTH = "cicCaseApplicantDateOfBirth";
    public static final String APPLICANT_ADDRESS = "cicCaseApplicantAddress";
    public static final String APPLICANT_PHONE_NUMBER = "cicCaseApplicantPhoneNumber";
    public static final String APPLICANT_CONTACT_PREFERENCE = "cicCaseApplicantContactDetailsPreference";

    public static final String REPRESENTATIVE_DETAILS = "representativeDetails";
    public static final String REPRESENTATIVE_REFERENCE = "cicCaseRepresentativeReference";
    public static final String REPRESENTATIVE_QUALIFIED = "cicCaseIsRepresentativeQualified";
    public static final String REPRESENTATIVE_ORG = "cicCaseRepresentativeOrgName";
    public static final String REPRESENTATIVE_FULLNAME = "cicCaseRepresentativeFullName";
    public static final String REPRESENTATIVE_PHONE_NUMBER = "cicCaseRepresentativePhoneNumber";
    public static final String REPRESENTATIVE_EMAIL = "cicCaseRepresentativeEmailAddress";
    public static final String REPRESENTATIVE_PRESENT = "cicCaseIsRepresentativePresent";
    public static final String REPRESENTATIVE_ADDRESS = "cicCaseRepresentativeAddress";
    public static final String REPRESENTATIVE_CONTACT_PREFERENCE = "cicCaseRepresentativeContactDetailsPreference";

    public static final String STAY_DETAILS = "stayDetails";
    public static final String IS_STAYED = "stayIsCaseStayed";
    public static final String STAY_REASON = "stayStayReason";
    public static final String STAY_EXPIRATION_DATE = "stayExpirationDate";
    public static final String STAY_ADDITIONAL_DETAILS = "stayAdditionalDetail";
    public static final String STAY_FLAG_TYPE = "stayFlagType";
    public static final String REMOVE_STAY_DETAILS = "removeStayDetails";
    public static final String REMOVE_STAY_REASON = "removeStayStayRemoveReason";
    public static final String REMOVE_STAY_OTHER_DESCRIPTION = "removeStayStayRemoveOtherDescription";
    public static final String REMOVE_STAY_ADDITIONAL_DETAIL = "removeStayAdditionalDetail";

    //Conditions
    public static final String COND_REPRESENTATIVE_NOT_EMPTY = "cicCaseRepresentativeFullName!=\"\"";
    public static final String COND_ALWAYS_HIDE_STAY_REASON = "stayStayReason=\"NEVER_SHOW\"";
    public static final String COND_IS_CASE_STAYED = "stayIsCaseStayed=\"Yes\"";
    public static final String COND_IS_NOT_CASE_STAYED = "stayIsCaseStayed=\"No\"";
    public static final String COND_HEARING_LIST_NOT_ANY_AND_CANCELLATION_REASON_NOT_EMPTY
        = "hearingList!=\"*\" AND cicCaseHearingCancellationReason!=\"\"";
    public static final String COND_HEARING_LIST_NOT_ANY_AND_CASE_POSTPONE_REASON_NOT_EMPTY
        = "hearingList!=\"*\" AND cicCasePostponeReason!=\"\"";
    public static final String COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY =
        "panel1=\"Tribunal Judge\" OR hearingList!=\"*\" AND isFullPanel!=\"\"";
    public static final String PANEL_COMPOSITION_DEFINED = "panel1=\"Tribunal Judge\"";
    public static final String COND_APPLICANT_FULL_NAME_NOT_EMPTY = "cicCaseApplicantFullName!=\"\"";
    public static final String COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY = "hearingList!=\"*\" AND hearingType!=\"\"";
    public static final String COND_REPRESENTATIVE_FULL_NAME_NOT_EMPTY = "cicCaseRepresentativeFullName!=\"\"";

    // required for Checkstyle
    private CaseFieldsConstants() {
    }
}
