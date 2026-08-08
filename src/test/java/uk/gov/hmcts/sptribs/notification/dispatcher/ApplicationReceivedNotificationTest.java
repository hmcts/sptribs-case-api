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
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ApplicationReceivedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ApplicationReceivedNotification applicationReceivedNotification;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(applicationReceivedNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            applicationReceivedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getEmail(),
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                TemplateName.APPLICATION_RECEIVED);
        }

        @Test
        void shouldNotifyApplicantOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setApplicantFullName("appFullName");
            data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setApplicantEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT))).thenReturn(NOTIFICATION_RESPONSE);

            applicationReceivedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getApplicantEmailAddress(),
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                TemplateName.APPLICATION_RECEIVED);
        }

        @Test
        void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setRepresentativeEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE))).thenReturn(NOTIFICATION_RESPONSE);

            applicationReceivedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRepresentativeEmailAddress(),
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                TemplateName.APPLICATION_RECEIVED);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(applicationReceivedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(applicationReceivedNotification, "citizenDashboardUrl", "https://sptribs.frontend/dashboard");
        }

        @Test
        void shouldNotifySubjectOfApplicationReceivedWithEmail() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), any(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            applicationReceivedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.APPLICATION_RECEIVED_NEW_CD));

            assertThat(templateVarsCaptor.getValue())
                .containsEntry(CommonConstants.DASHBOARD_KEY, "https://sptribs.frontend/dashboard");
        }
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
