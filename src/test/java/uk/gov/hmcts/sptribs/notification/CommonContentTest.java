package uk.gov.hmcts.sptribs.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfig;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.FEMALE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.MALE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.sptribs.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.sptribs.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.WIFE_JOINT;
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
}
