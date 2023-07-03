package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;

@ExtendWith(MockitoExtension.class)
class DssNotificationHelperTest {

    @InjectMocks
    private DssNotificationHelper dssNotificationHelper;


    @Test
    void shouldGetSubjectCommonVars() {
        // Given
        DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("subject name")
            .build();

        CicCase cicCase = CicCase.builder()
            .fullName(dssCaseData.getSubjectFullName())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        // When
        Map<String, Object> commonVars = dssNotificationHelper.getSubjectCommonVars("case number", caseData);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("subject name");
    }


    @Test
    void shouldGetReprCommonVars() {
        // Given
        DssCaseData dssCaseData = DssCaseData.builder()
            .representativeFullName("repr name")
            .build();

        CicCase cicCase = CicCase.builder()
            .representativeFullName(dssCaseData.getRepresentativeFullName())
            .build();
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        // When
        Map<String, Object> commonVars = dssNotificationHelper.getRepresentativeCommonVars("case number", caseData);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("repr name");
    }


    @Test
    void shouldBuildNotificationRequest() {
        // When
        NotificationRequest emailNotificationRequest = dssNotificationHelper.buildEmailNotificationRequest(
            "id@email.com",
            new HashMap<>(),
            TemplateName.APPLICATION_RECEIVED);

        // Then
        assertThat(emailNotificationRequest).isNotNull();
    }

}
