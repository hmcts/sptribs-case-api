package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.ApplicationReceivedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_NEW_CD;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ApplicationReceivedNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private ApplicationReceivedNotification applicationReceivedNotification;

    @Autowired
    private Environment environment;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void dashboardFeaturePropertyExplicitlyConfigured() {
        assertThat(
            environment.getProperty("feature.citizen-dashboard.enabled"))
            .as("feature.citizen-dashboard.enabled must be set in application-integration.yaml")
            .isNotNull();
    }

    @Nested
    class WhenCitizenDashboardDisabled {

        @Autowired
        Environment environment;

        @BeforeEach
        void onlyRunWhenDisabled() {
            assumeFalse(environment.getProperty("feature.citizen-dashboard.enabled", Boolean.class, false),
                "Skipping: feature.citizen-dashboard.enabled is currently true");
        }

        @Test
        void shouldSendEmailToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Test Name")
                    .email("subject@email.com")
                    .build())
                .build();

            applicationReceivedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(APPLICATION_RECEIVED);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Test Name",
                    CONTACT_NAME, "Test Name"
                ))
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldSendEmailToApplicant() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .applicantContactDetailsPreference(EMAIL)
                    .fullName("Test Name")
                    .applicantFullName("Applicant Name")
                    .applicantEmailAddress("applicant@email.com")
                    .build())
                .build();

            applicationReceivedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("applicant@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(APPLICATION_RECEIVED);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Test Name",
                    CONTACT_NAME, "Applicant Name"
                ))
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldSendEmailToRepresentative() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Test Name")
                    .representativeContactDetailsPreference(EMAIL)
                    .representativeFullName("Representative Name")
                    .representativeEmailAddress("representative@email.com")
                    .build())
                .build();

            applicationReceivedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(APPLICATION_RECEIVED);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Test Name",
                    CONTACT_NAME, "Representative Name"
                ))
                .doesNotContainKey(DASHBOARD_KEY);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {

        @Autowired
        Environment environment;

        @BeforeEach
        void onlyRunWhenEnabled() {
            assumeTrue(environment.getProperty("feature.citizen-dashboard.enabled", Boolean.class, false),
                "Skipping: feature.citizen-dashboard.enabled is currently false");
        }

        @Test
        void shouldSendEmailToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Test Name")
                    .email("subject@email.com")
                    .build())
                .build();

            applicationReceivedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(APPLICATION_RECEIVED_NEW_CD);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Test Name",
                    CONTACT_NAME, "Test Name"
                ))
                .containsKey(DASHBOARD_KEY);
        }

        @Test
        void shouldSendEmailToApplicant() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .applicantContactDetailsPreference(EMAIL)
                    .fullName("Test Name")
                    .applicantFullName("Applicant Name")
                    .applicantEmailAddress("applicant@email.com")
                    .build())
                .build();

            applicationReceivedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("applicant@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(APPLICATION_RECEIVED_NEW_CD);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Test Name",
                    CONTACT_NAME, "Applicant Name"
                ))
                .containsKey(DASHBOARD_KEY);
        }

        @Test
        void shouldSendEmailToRepresentative() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Test Name")
                    .representativeContactDetailsPreference(EMAIL)
                    .representativeFullName("Representative Name")
                    .representativeEmailAddress("representative@email.com")
                    .build())
                .build();

            applicationReceivedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), eq(null));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress())
                .isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate())
                .isEqualTo(APPLICATION_RECEIVED_NEW_CD);
            assertThat(notificationRequest.getTemplateVars())
                .containsAllEntriesOf(Map.of(
                    TRIBUNAL_NAME, CIC,
                    CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                    CIC_CASE_SUBJECT_NAME, "Test Name",
                    CONTACT_NAME, "Representative Name"
                ))
                .containsKey(DASHBOARD_KEY);
        }
    }
}
