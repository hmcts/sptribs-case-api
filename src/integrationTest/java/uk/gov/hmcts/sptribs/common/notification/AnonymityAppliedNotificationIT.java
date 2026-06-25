package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.AnonymityAppliedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.ANONYMITY_APPLIED_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AnonymityAppliedNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private AnonymityAppliedNotification anonymityAppliedNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .contactPreferenceType(ContactPreferenceType.EMAIL)
                .email("subject@test.com")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name"
        );

        anonymityAppliedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress()).isEqualTo("subject@test.com");
        assertThat(notificationRequest.getTemplate()).isEqualTo(ANONYMITY_APPLIED_EMAIL);
        assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .representativeFullName("Representative Name")
                .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
                .representativeEmailAddress("rep@test.com")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name"
        );

        anonymityAppliedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress()).isEqualTo("rep@test.com");
        assertThat(notificationRequest.getTemplate()).isEqualTo(ANONYMITY_APPLIED_EMAIL);
        assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(expectedTemplateVars);
    }
}
