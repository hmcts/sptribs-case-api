package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.BundleCreatedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN;
import static uk.gov.hmcts.sptribs.notification.TemplateName.BUNDLE_CREATED_EMAIL_RESPONDENT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BundleCreatedNotificationIT {

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Nested
    @TestPropertySource(properties = "feature.citizen-dashboard.enabled=false")
    class WhenCitizenDashboardDisabled {
        @Autowired
        private BundleCreatedNotification bundleCreatedNotification;

        @MockitoBean
        private NotificationServiceCIC notificationServiceCIC;

        @Test
        void shouldSendEmailToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Subject Name")
                    .email("subject@email.com")
                    .build())
                .build();

            final NotificationResponse expectedResponse = NotificationResponse.builder().build();
            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null)))
                .thenReturn(expectedResponse);

            bundleCreatedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_CITIZEN);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Subject Name"
                ));
            assertThat(notificationRequest.getTemplateVars()).doesNotContainKey(DASHBOARD_KEY);
            assertThat(data.getCicCase().getSubjectNotifyList()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldSendEmailToRepresentative() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .representativeFullName("Representative Name")
                    .representativeEmailAddress("representative@email.com")
                    .build())
                .build();

            bundleCreatedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_CITIZEN);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_REPRESENTATIVE_NAME, "Representative Name"
                ));
        }

        @Test
        void shouldSendEmailToApplicant() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .applicantFullName("Applicant Name")
                    .applicantEmailAddress("applicant@email.com")
                    .build())
                .build();

            bundleCreatedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("applicant@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_CITIZEN);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_APPLICANT_NAME, "Applicant Name"
                ));
        }

        @Test
        void shouldSendEmailToRespondent() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .build())
                .build();

            final NotificationResponse expectedResponse = NotificationResponse.builder().build();
            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null)))
                .thenReturn(expectedResponse);

            bundleCreatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("respondent@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_RESPONDENT);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CONTACT_NAME, "Respondent Name"
                ));
            assertThat(data.getCicCase().getResNotificationResponse()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldSendEmailToRespondentWithFallbackWhenRespondentEmailIsNull() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .respondentName("Respondent Name")
                    .respondentEmail(null)
                    .build())
                .build();

            final NotificationResponse expectedResponse = NotificationResponse.builder().build();
            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null)))
                .thenReturn(expectedResponse);

            bundleCreatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("appeals.team@cica.gov.uk");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_RESPONDENT);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CONTACT_NAME, "Respondent Name"
                ));
            assertThat(data.getCicCase().getResNotificationResponse()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldSendEmailToRespondentWithFallbackWhenRespondentEmailIsBlank() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .respondentName("Respondent Name")
                    .respondentEmail("   ")
                    .build())
                .build();

            final NotificationResponse expectedResponse = NotificationResponse.builder().build();
            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null)))
                .thenReturn(expectedResponse);

            bundleCreatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("appeals.team@cica.gov.uk");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_RESPONDENT);
            assertThat(data.getCicCase().getResNotificationResponse()).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @TestPropertySource(properties = """
            feature.citizen-dashboard.enabled=true
            sptribs-frontend.dashboard-url=https://frontend.url/dashboard
        """)
    class WhenCitizenDashboardEnabled {
        @Autowired
        private BundleCreatedNotification bundleCreatedNotification;

        @MockitoBean
        private NotificationServiceCIC notificationServiceCIC;

        @Test
        void shouldSendEmailToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Subject Name")
                    .email("subject@email.com")
                    .build())
                .build();

            final NotificationResponse expectedResponse = NotificationResponse.builder().build();
            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null)))
                .thenReturn(expectedResponse);

            bundleCreatedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_CITIZEN);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Subject Name",
                    DASHBOARD_KEY, "https://frontend.url/dashboard"
                ));
            assertThat(data.getCicCase().getSubjectNotifyList()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldSendEmailToRepresentative() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .representativeFullName("Representative Name")
                    .representativeEmailAddress("representative@email.com")
                    .build())
                .build();

            bundleCreatedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(BUNDLE_CREATED_EMAIL_CITIZEN);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_REPRESENTATIVE_NAME, "Representative Name",
                    DASHBOARD_KEY, "https://frontend.url/dashboard"
                ));
        }
    }
}
