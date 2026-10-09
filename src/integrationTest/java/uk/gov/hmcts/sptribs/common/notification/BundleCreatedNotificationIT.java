package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.BundleCreatedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
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
        void shouldSendEmailToConfiguredRespondentInsteadOfStoredAddress() {
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
        void shouldSendEmailToRespondentWhenStoredAddressIsNull() {
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
        void shouldSendEmailToRespondentWhenStoredAddressIsBlank() {
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

        @Test
        void shouldDispatchToEveryEligiblePartyAndUseConfiguredCicaInbox() {
            CaseData data = CaseData.builder().cicCase(CicCase.builder()
                .contactPreferenceType(POST)
                .caseSubcategory(CaseSubcategory.OTHER)
                .fullName("Subject")
                .email("subject@example.com")
                .respondentEmail(" ")
                .respondentName("Respondent")
                .partiesCIC(Set.of(PartiesCIC.REPRESENTATIVE, PartiesCIC.APPLICANT))
                .representativeFullName("Representative")
                .representativeEmailAddress("representative@example.com")
                .applicantFullName("Applicant")
                .applicantEmailAddress("applicant@example.com")
                .build()).build();

            BundleCreatedNotification.DispatchResult result = bundleCreatedNotification.dispatch(data, TEST_CASE_ID.toString());

            assertThat(result.sentParties()).containsExactlyInAnyOrder(SUBJECT, RESPONDENT, REPRESENTATIVE, APPLICANT);
            assertThat(result.failedParties()).isEmpty();
            ArgumentCaptor<NotificationRequest> requests = ArgumentCaptor.forClass(NotificationRequest.class);
            verify(notificationServiceCIC, times(4)).sendEmail(requests.capture(), eq(TEST_CASE_ID.toString()), isNull());
            assertThat(requests.getAllValues()).extracting(NotificationRequest::getDestinationAddress)
                .containsExactly("subject@example.com", "appeals.team@cica.gov.uk",
                    "representative@example.com", "applicant@example.com");
        }

        @ParameterizedTest
        @EnumSource(value = CaseSubcategory.class, names = {"FATAL", "MINOR"})
        void shouldSuppressSubjectForFatalAndMinorCases(CaseSubcategory subcategory) {
            CaseData data = CaseData.builder().cicCase(CicCase.builder()
                .caseSubcategory(subcategory).email("subject@example.com").build()).build();

            BundleCreatedNotification.DispatchResult result = bundleCreatedNotification.dispatch(data, TEST_CASE_ID.toString());

            assertThat(result.sentParties()).containsExactly(RESPONDENT);
            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), isNull());
            assertThat(notificationRequestCaptor.getValue().getDestinationAddress()).isEqualTo("appeals.team@cica.gov.uk");
        }

        @Test
        void shouldSkipMissingAndUnselectedPartyAddressesButStillNotifyCica() {
            CaseData data = CaseData.builder().cicCase(CicCase.builder()
                .email(" ")
                .partiesCIC(Set.of(PartiesCIC.APPLICANT))
                .representativeEmailAddress("representative@example.com")
                .applicantEmailAddress("")
                .build()).build();

            BundleCreatedNotification.DispatchResult result = bundleCreatedNotification.dispatch(data, TEST_CASE_ID.toString());

            assertThat(result.sentParties()).containsExactly(RESPONDENT);
            assertThat(result.failedParties()).isEmpty();
            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()), isNull());
            assertThat(notificationRequestCaptor.getValue().getDestinationAddress()).isEqualTo("appeals.team@cica.gov.uk");
        }

        @Test
        void shouldContinueSendingAndReportEachFailedPartyWithoutUsingOldResponses() {
            CaseData data = CaseData.builder().cicCase(CicCase.builder()
                .email("subject@example.com")
                .subjectNotifyList(NotificationResponse.builder().build())
                .respondentEmail(null)
                .partiesCIC(Set.of(PartiesCIC.REPRESENTATIVE, PartiesCIC.APPLICANT))
                .representativeEmailAddress("representative@example.com")
                .applicantEmailAddress("applicant@example.com")
                .build()).build();
            doThrow(new IllegalStateException("Notification failed"))
                .when(notificationServiceCIC).sendEmail(argThat(request -> request != null
                    && Set.of("subject@example.com", "representative@example.com")
                        .contains(request.getDestinationAddress())), eq(TEST_CASE_ID.toString()), isNull());

            BundleCreatedNotification.DispatchResult result = bundleCreatedNotification.dispatch(data, TEST_CASE_ID.toString());

            assertThat(result.sentParties()).containsExactlyInAnyOrder(RESPONDENT, APPLICANT);
            assertThat(result.failedParties()).containsExactly(SUBJECT, REPRESENTATIVE);
            verify(notificationServiceCIC, times(4)).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), isNull());
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
