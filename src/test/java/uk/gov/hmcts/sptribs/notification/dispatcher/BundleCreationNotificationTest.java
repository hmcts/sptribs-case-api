package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
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
        void shouldNotifyConfiguredRespondentThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRespondentEmail("testresp@outlook.com");
            ReflectionTestUtils.setField(bundleCreatedNotification, "configuredRespondentEmail", "appeals.team@cica.gov.uk");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq("appeals.team@cica.gov.uk"),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_RESPONDENT));
            assertThat(templateVarsCaptor.getValue()).containsEntry("CicCaseRespondentFullName","Appeals team");
        }

        @Test
        void shouldNotifySubjectThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setEmail("testsubject@outlook.com");
            data.getCicCase().setFullName("Subject FullName");

            //When
            when(notificationService.sendEmail(any(), any(), any()))
                .thenReturn(NotificationResponse.builder().build());
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq("testsubject@outlook.com"),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN));
            assertThat(templateVarsCaptor.getValue()).containsEntry("CicCaseSubjectFullName", "Subject FullName");
            assertThat(data.getCicCase().getSubjectNotifyList()).isNotNull();
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

        @Test
        void shouldNotifySubjectThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setEmail("testsubject@outlook.com");
            data.getCicCase().setFullName("Subject FullName");

            //When
            when(notificationService.sendEmail(any(), any(), any()))
                .thenReturn(NotificationResponse.builder().build());
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq("testsubject@outlook.com"),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN));
            assertThat(templateVarsCaptor.getValue())
                .containsEntry("CicCaseSubjectFullName", "Subject FullName")
                .containsEntry(DASHBOARD_KEY, "https://frontend.url/dashboard");
            assertThat(data.getCicCase().getSubjectNotifyList()).isNotNull();
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
            assertThat(templateVarsCaptor.getValue())
                .containsEntry("CicCaseRepresentativeFullName", "Rep LastName")
                .containsEntry(DASHBOARD_KEY, "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifyConfiguredRespondentThatBundleIsCreated() {
            //Given
            final CaseData data = getMockCaseData();
            data.getCicCase().setRespondentEmail("testresp@outlook.com");
            ReflectionTestUtils.setField(bundleCreatedNotification, "configuredRespondentEmail", "appeals.team@cica.gov.uk");

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            bundleCreatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq("appeals.team@cica.gov.uk"),
                templateVarsCaptor.capture(),
                eq(TemplateName.BUNDLE_CREATED_EMAIL_RESPONDENT));
            assertThat(templateVarsCaptor.getValue())
                .containsEntry("CicCaseRespondentFullName", "Appeals team")
                .containsEntry(DASHBOARD_KEY, "https://frontend.url/dashboard");
        }
    }

    @Nested
    class Dispatch {
        private static final String RESPONDENT_INBOX = "respondent@example.com";

        private BundleCreatedNotification dispatchNotification;

        @BeforeEach
        void setUp() {
            dispatchNotification = new BundleCreatedNotification(notificationService, new NotificationHelper());
            ReflectionTestUtils.setField(dispatchNotification, "configuredRespondentEmail", RESPONDENT_INBOX);
        }

        @Test
        void shouldSendToAllEligiblePartiesAndStoreResponses() {
            CicCase cicCase = CicCase.builder()
                .caseSubcategory(CaseSubcategory.OTHER)
                .email("subject@example.com")
                .partiesCIC(Set.of(PartiesCIC.REPRESENTATIVE, PartiesCIC.APPLICANT))
                .representativeEmailAddress("representative@example.com")
                .applicantEmailAddress("applicant@example.com")
                .build();
            NotificationResponse response = NotificationResponse.builder().build();
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), isNull()))
                .thenReturn(response);

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertThat(result.sentParties()).containsExactlyInAnyOrder(SUBJECT, RESPONDENT, REPRESENTATIVE, APPLICANT);
            assertThat(result.failedParties()).isEmpty();
            assertThat(cicCase.getSubjectNotifyList()).isSameAs(response);
            assertThat(cicCase.getResNotificationResponse()).isSameAs(response);
            assertThat(cicCase.getRepNotificationResponse()).isSameAs(response);
            assertThat(cicCase.getAppNotificationResponse()).isSameAs(response);
            ArgumentCaptor<NotificationRequest> requests = ArgumentCaptor.forClass(NotificationRequest.class);
            verify(notificationService, times(4)).sendEmail(requests.capture(), eq(TEST_CASE_ID.toString()), isNull());
            assertThat(requests.getAllValues()).extracting(NotificationRequest::getDestinationAddress)
                .containsExactly("subject@example.com", RESPONDENT_INBOX,
                    "representative@example.com", "applicant@example.com");
        }

        @ParameterizedTest
        @EnumSource(value = CaseSubcategory.class, names = {"FATAL", "MINOR"})
        void shouldNotNotifySubjectForExcludedSubcategories(CaseSubcategory subcategory) {
            CicCase cicCase = CicCase.builder().caseSubcategory(subcategory).email("subject@example.com").build();

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertOnlyRespondentNotified(result);
        }

        @Test
        void shouldSkipSubjectWithBlankEmailAndAbsentParties() {
            CicCase cicCase = CicCase.builder().caseSubcategory(CaseSubcategory.OTHER).email(" ").build();

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertOnlyRespondentNotified(result);
        }

        @Test
        void shouldSkipPartiesWithoutAddresses() {
            CicCase cicCase = CicCase.builder()
                .partiesCIC(Set.of(PartiesCIC.REPRESENTATIVE, PartiesCIC.APPLICANT))
                .representativeEmailAddress(" ")
                .applicantEmailAddress("")
                .build();

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertOnlyRespondentNotified(result);
        }

        @Test
        void shouldSkipUnselectedPartiesEvenWhenTheyHaveAddresses() {
            CicCase cicCase = CicCase.builder()
                .partiesCIC(Set.of())
                .representativeEmailAddress("representative@example.com")
                .applicantEmailAddress("applicant@example.com")
                .build();

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertOnlyRespondentNotified(result);
        }

        @Test
        void shouldContinueAfterSubjectAndRepresentativeFailures() {
            CicCase cicCase = CicCase.builder()
                .email("subject@example.com")
                .partiesCIC(Set.of(PartiesCIC.REPRESENTATIVE, PartiesCIC.APPLICANT))
                .representativeEmailAddress("representative@example.com")
                .applicantEmailAddress("applicant@example.com")
                .build();
            doThrow(new IllegalStateException("Notification failed"))
                .when(notificationService).sendEmail(argThat(request -> request != null
                    && Set.of("subject@example.com", "representative@example.com")
                        .contains(request.getDestinationAddress())), eq(TEST_CASE_ID.toString()), isNull());

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertThat(result.sentParties()).containsExactlyInAnyOrder(RESPONDENT, APPLICANT);
            assertThat(result.failedParties()).containsExactly(SUBJECT, REPRESENTATIVE);
            verify(notificationService, times(4)).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), isNull());
        }

        @Test
        void shouldReportRespondentAndApplicantFailures() {
            CicCase cicCase = CicCase.builder()
                .partiesCIC(Set.of(PartiesCIC.APPLICANT))
                .applicantEmailAddress("applicant@example.com")
                .build();
            doThrow(new IllegalStateException("Notification failed"))
                .when(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), isNull());

            BundleCreatedNotification.DispatchResult result = dispatchNotification.dispatch(caseData(cicCase), TEST_CASE_ID.toString());

            assertThat(result.sentParties()).isEmpty();
            assertThat(result.failedParties()).containsExactly(RESPONDENT, APPLICANT);
            verify(notificationService, times(2)).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), isNull());
        }

        private void assertOnlyRespondentNotified(BundleCreatedNotification.DispatchResult result) {
            assertThat(result.sentParties()).containsExactly(RESPONDENT);
            assertThat(result.failedParties()).isEmpty();
            ArgumentCaptor<NotificationRequest> request = ArgumentCaptor.forClass(NotificationRequest.class);
            verify(notificationService).sendEmail(request.capture(), eq(TEST_CASE_ID.toString()), isNull());
            assertThat(request.getValue().getDestinationAddress()).isEqualTo(RESPONDENT_INBOX);
        }

        private CaseData caseData(CicCase cicCase) {
            return CaseData.builder().cicCase(cicCase).build();
        }
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();

        return CaseData.builder().cicCase(cicCase).build();
    }
}
