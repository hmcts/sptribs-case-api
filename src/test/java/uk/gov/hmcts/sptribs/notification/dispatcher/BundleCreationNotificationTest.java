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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class BundleCreationNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private BundleCreatedNotification bundleCreatedNotification;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(bundleCreatedNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifyApplicantThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setApplicantEmailAddress("testapp@outlook.com");
            data.getCicCase().setApplicantFullName("Applicant LastName");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getApplicantEmailAddress()),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN));
            assertThat(templateVarsCaptor.getValue()).containsEntry("CicCaseApplicantFullName", "Applicant LastName");
        }

        @Test
        void shouldNotifyRepresentativeThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
            data.getCicCase().setRepresentativeFullName("Rep LastName");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getRepresentativeEmailAddress()),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN));
            assertThat(templateVarsCaptor.getValue()).containsEntry("CicCaseRepresentativeFullName", "Rep LastName");
        }

        @Test
        void shouldNotifyRespondentThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRespondentEmail("testresp@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getRespondentEmail()),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_RESPONDENT));
            assertThat(templateVarsCaptor.getValue()).containsEntry("CicCaseRespondentFullName","Appeals team");
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(bundleCreatedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(bundleCreatedNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifyApplicantThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setApplicantEmailAddress("testapp@outlook.com");
            data.getCicCase().setApplicantFullName("Applicant LastName");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getApplicantEmailAddress()),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN));
            assertThat(templateVarsCaptor.getValue())
                .containsEntry("CicCaseApplicantFullName", "Applicant LastName")
                .containsEntry(DASHBOARD_KEY, "https://frontend.url/dashboard");
        }

    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();

        return CaseData.builder().cicCase(cicCase).build();
    }
}
