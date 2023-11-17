package uk.gov.hmcts.sptribs.ciccase.search;

public final class CaseFieldsConstants {

    public static final String SUBJECT_NAME = "cicCaseFullName";
    public static final String HEARING_DATE = "firstHearingDate";
    public static final String SUBJECT_DATE_OF_BIRTH = "cicCaseDateOfBirth";
    public static final String REPRESENTATIVE_REFERENCE = "cicCaseRepresentativeReference";
    public static final String SUBJECT_ADDRESS = "cicCaseAddress";
    public static final String HEARING_LOCATION = "hearingVenueName";
    public static final String CASE_CATEGORY = "cicCaseCaseCategory";
    public static final String CASE_SUBCATEGORY = "cicCaseCaseSubcategory";
    public static final String APPLICANT_NAME = "cicCaseApplicantFullName";
    public static final String CCD_REFERENCE = "[CASE_REFERENCE]";
    public static final String CASE_STATE = "[STATE]";
    public static final String CASE_REGION = "cicCaseRegionCIC";
    public static final String DUE_DATE = "cicCaseFirstDueDate";

    public static final String SCHEME = "cicCaseSchemeCic";
    public static final String LAST_MODIFIED_DATE = "[LAST_MODIFIED_DATE]";
    public static final String LAST_STATE_MODIFIED_DATE = "[LAST_STATE_MODIFIED_DATE]";

    // required for Checkstyle
    private CaseFieldsConstants() {
    }

}
