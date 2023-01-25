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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

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
