package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ApplicationReceivedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ApplicationReceivedNotification applicationReceivedNotification;

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(applicationReceivedNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            applicationReceivedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getEmail(),
                new HashMap<>(),
                TemplateName.APPLICATION_RECEIVED);
        }

        @Test
        void shouldNotNotifySubjectOfApplicationIfContactPreferenceIsNull() {
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(null);
            data.getCicCase().setEmail("test@outlook.com");

            applicationReceivedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationService, never()).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
        }

        @Test
        void shouldNotifyApplicantOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setApplicantFullName("appFullName");
            data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setApplicantEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            applicationReceivedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getApplicantEmailAddress(),
                new HashMap<>(),
                TemplateName.APPLICATION_RECEIVED);
        }


        @Test
        void shouldNotNotifyApplicantOfApplicationIfContactPreferenceIsNull() {
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(null);
            data.getCicCase().setEmail("test@outlook.com");

            applicationReceivedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationService, never()).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
        }

        @Test
        void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setRepresentativeEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            applicationReceivedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRepresentativeEmailAddress(),
                new HashMap<>(),
                TemplateName.APPLICATION_RECEIVED);
        }


        @Test
        void shouldNotNotifyRepresentativeOfApplicationIfContactPreferenceIsNull() {
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(null);
            data.getCicCase().setEmail("test@outlook.com");

            applicationReceivedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationService, never()).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {
        @Captor
        private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(applicationReceivedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(applicationReceivedNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setRepresentativeEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            applicationReceivedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getRepresentativeEmailAddress()),
                templateVarsCaptor.capture(),
                eq(TemplateName.APPLICATION_RECEIVED_NEW_CD));
            assertThat(templateVarsCaptor.getValue()).containsEntry(DASHBOARD_KEY, "https://frontend.url/dashboard");
        }
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
