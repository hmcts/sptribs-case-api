package uk.gov.hmcts.sptribs.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfig;

import java.util.Map;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.FEMALE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.MALE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.sptribs.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.sptribs.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.WIFE_JOINT;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class CommonContentTest {

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private CommonContent commonContent;

    @Test
    void shouldSetCommonTemplateVarsForDivorceNotifications() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));

        //When
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, TEST_CASE_ID);

        //Then
        assertThat(templateVars).isNotEmpty().hasSize(4)
            .contains(
                entry(COURT_EMAIL, "divorce.court@email.com"),
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldSetCommonTemplateVarsForDissolutionNotifications() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        caseData.setDivorceOrDissolution(DISSOLUTION);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DISSOLUTION_COURT_EMAIL, "dissolution.court@email.com"));

        //When
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, TEST_CASE_ID);

        //Then
        assertThat(templateVars).isNotEmpty().hasSize(4)
            .contains(
                entry(COURT_EMAIL, "dissolution.court@email.com"),
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldGetPartnerAsWife() {
        //When
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        //Then
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("wife");
    }

    @Test
    void shouldGetPartnerAsHusband() {
        //When
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(MALE);
        //Then
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("husband");
    }

    @Test
    void shouldGetPartnerAsSpouse() {
        //When
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(null);
        //Then
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("spouse");
    }

    @Test
    void shouldGetPartnerAsCivilPartner() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        //Then
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("civil partner");
    }

    @Test
    void shouldGetPartnerWelshContentAsGwraig() {
        //When
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        //Then
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("gwraig");
    }

    @Test
    void shouldGetPartnerWelshContent() {
        //When
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(MALE);
        //Then
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("gŵr");
    }

    @Test
    void shouldGetPartnerWelshContentAsPriod() {
        //When
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(null);
        //Then
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("priod");
    }

    @Test
    void shouldGetPartnerWelshContentAsPartnerSifil() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        //Then
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("partner sifil");
    }

    @Test
    void shouldGetUnionTypeDivorce() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        //Then
        assertThat(commonContent.getUnionType(caseData)).isEqualTo("divorce");
    }

    @Test
    void shouldGetUnionTypeDissolution() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        //Then
        assertThat(commonContent.getUnionType(caseData)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetEnglishUnionTypeDivorce() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        //Then
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.ENGLISH)).isEqualTo("divorce");
    }

    @Test
    void shouldGetEnglishUnionTypeDissolution() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        //Then
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.ENGLISH)).isEqualTo("dissolution");

    }

    @Test
    void shouldGetWelshUnionTypeYsgariad() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        //Then
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("ysgariad");
    }

    @Test
    void shouldGetWelshUnionTypeDiddymiad() {
        //When
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        //Then
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("diddymiad");
    }

    @Test
    void shouldSetTemplateVarsForSoleApplication() {
        //Given
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        //When
        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(), respondent());

        //Then
        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "no"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDivorceApplicationWhenPartnerIsMale() {
        //Given
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        //When
        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(FEMALE), getApplicant(MALE));

        //Then
        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "yes"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDivorceApplicationWhenPartnerIsFemale() {
        //Given
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        //When
        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(MALE), getApplicant(FEMALE));

        //Then
        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "yes"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDissolution() {
        //Given
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .build();

        //When
        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(MALE), getApplicant(FEMALE));

        //Then
        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "yes")
            );
    }

    @Test
    void shouldReturnProfessionalSignInUrl() {
        //Given
        Long caseId = 123456789L;
        when(emailTemplatesConfig.getTemplateVars())
            .thenReturn(Map.of(SIGN_IN_PROFESSIONAL_USERS_URL, "http://professional-sing-in-url/"));

        //When
        String professionalSignInUrl = commonContent.getProfessionalUsersSignInUrl(caseId);

        //Then
        assertThat(professionalSignInUrl).isEqualTo("http://professional-sing-in-url/123456789");
    }

    @Test
    void shouldAddWelshPartnerContentIfApplicant1PrefersWelsh() {
        //Given
        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(YES)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        //When
        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant1, applicant2);

        //Then
        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "gwraig")
            );
    }

    @Test
    void shouldNotAddWelshPartnerContentIfApplicant1DoesNotPreferWelsh() {
        //Given
        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        //When
        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant1, applicant2);

        //Then
        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "wife")
            );
    }

    @Test
    void shouldAddWelshPartnerContentIfApplicant2PrefersWelsh() {
        //Given
        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .languagePreferenceWelsh(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        //When
        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant2, applicant1);

        //Then
        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "gŵr")
            );
    }

    @Test
    void shouldNotAddWelshPartnerContentIfApplicant2DoesNotPreferWelsh() {
        //Given
        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        //When
        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant2, applicant1);

        //Then
        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "husband")
            );
    }
}
