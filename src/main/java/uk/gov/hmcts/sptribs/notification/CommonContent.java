package uk.gov.hmcts.sptribs.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfig;

import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;

@Component
public class CommonContent {

    public static final String PARTNER = "partner";
    public static final String FIRST_NAME = "first name";
    public static final String LAST_NAME = "last name";

    public static final String IS_DIVORCE = "isDivorce";
    public static final String IS_DISSOLUTION = "isDissolution";

    public static final String IS_REMINDER = "isReminder";
    public static final String YES = "yes";
    public static final String NO = "no";

    public static final String IS_PAID = "isPaid";

    public static final String CREATE_ACCOUNT_LINK = "create account link";
    public static final String SIGN_IN_URL = "signin url";
    public static final String SIGN_IN_DIVORCE_URL = "signInDivorceUrl";
    public static final String SIGN_IN_DISSOLUTION_URL = "signInDissolutionUrl";
    public static final String SIGN_IN_PROFESSIONAL_USERS_URL = "signInProfessionalUsersUrl";
    public static final String DIVORCE_COURT_EMAIL = "divorceCourtEmail";
    public static final String DISSOLUTION_COURT_EMAIL = "dissolutionCourtEmail";

    public static final String SUBMISSION_RESPONSE_DATE = "date of response";
    public static final String APPLICATION_REFERENCE = "reference number";
    public static final String IS_UNDISPUTED = "isUndisputed";
    public static final String IS_DISPUTED = "isDisputed";
    public static final String DATE_OF_ISSUE = "date of issue";

    public static final String ACCESS_CODE = "access code";

    public static final String APPLICANT_NAME = "applicant name";
    public static final String RESPONDENT_NAME = "respondent name";
    public static final String SOLICITOR_NAME = "solicitor name";
    public static final String SOLICITOR_REFERENCE = "solicitor reference";
    public static final String SOLICITOR_FIRM = "solicitor firm";

    public static final String REVIEW_DEADLINE_DATE = "review deadline date";

    public static final String JOINT_CONDITIONAL_ORDER = "joint conditional order";
    public static final String HUSBAND_JOINT = "husbandJoint";
    public static final String WIFE_JOINT = "wifeJoint";
    public static final String CIVIL_PARTNER_JOINT = "civilPartnerJoint";

    public static final String ISSUE_DATE = " issue date";

    public static final String UNION_TYPE = "union type";

    public static final String COURT_NAME = "court name";
    public static final String COURT_EMAIL = "court email";
    public static final String DATE_OF_HEARING = "date of hearing";
    public static final String TIME_OF_HEARING = "time of hearing";
    public static final String DATE_OF_HEARING_MINUS_SEVEN_DAYS = "date of hearing minus seven days";
    public static final String CO_PRONOUNCEMENT_DATE_PLUS_43 = "CO pronouncement date plus 43 days";

    public static final String DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS = "date final order eligible from plus 3 months";

    public static final String IS_SOLE = "isSole";
    public static final String IS_JOINT = "isJoint";

    public static final String DIVORCE = "divorce";
    public static final String DISSOLUTION = "dissolution";
    public static final String DIVORCE_WELSH = "ysgariad";
    public static final String DISSOLUTION_WELSH = "diddymiad";

    @Autowired
    private EmailTemplatesConfig config;

    public String getUnionType(CaseData caseData, LanguagePreference applicantLanguagePreference) {
        if (WELSH.equals(applicantLanguagePreference)) {
            return caseData.isDivorce() ? DIVORCE_WELSH : DISSOLUTION_WELSH;
        }

        return caseData.isDivorce() ? DIVORCE : DISSOLUTION;
    }

    public String getUnionType(CaseData caseData) {
        return caseData.isDivorce() ? DIVORCE : DISSOLUTION;
    }

    public String getSignInUrl(CaseData caseData) {
        return config.getTemplateVars().get(caseData.isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL);
    }

    public String getProfessionalUsersSignInUrl(Long caseId) {
        return config.getTemplateVars().get(SIGN_IN_PROFESSIONAL_USERS_URL) + caseId;
    }
}
