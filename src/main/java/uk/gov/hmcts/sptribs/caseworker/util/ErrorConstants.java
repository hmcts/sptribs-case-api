package uk.gov.hmcts.sptribs.caseworker.util;

public final class ErrorConstants {

    public static final String SELECT_AT_LEAST_ONE_CONTACT_PARTY
        = "Please select at least one party to contact";
    public static final String MINOR_FATAL_SUBJECT_ERROR_MESSAGE
        = "Subject should not be selected for notification if the case is Fatal or Minor";
    public static final String SELECT_AT_LEAST_ONE_ERROR_MESSAGE
        = "One recipient must be selected.";
    public static final String INCOMPATIBLE_REFERRAL_REASON = "The case state is incompatible with the selected referral reason";
    public static final String FAILED_TO_ANONYMISE_CASE = "Failed to generate an anonymised name for the case";

    private ErrorConstants() {

    }
}
