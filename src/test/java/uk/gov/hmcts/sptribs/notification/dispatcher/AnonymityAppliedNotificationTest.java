package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.notification.NotificationConstants;
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

@ExtendWith(MockitoExtension.class)
class AnonymityAppliedNotificationTest {

    @Mock
    private NotificationServiceCIC notificationServiceCIC;

    @Mock
    private NotificationHelper notificationHelper;

    private AnonymityAppliedNotification anonymityAppliedNotification;

    @BeforeEach
    void setUp() {
        anonymityAppliedNotification = new AnonymityAppliedNotification(
            notificationServiceCIC,
            notificationHelper
        );
    }

    @Test
    void shouldSendAnonymityAppliedEmailToSubject() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Test Subject")
                .email("subject@test.com")
                .contactPreferenceType(ContactPreferenceType.EMAIL)
                .build())
            .build();

        Map<String, Object> templateVars = new HashMap<>();
        NotificationResponse response = NotificationResponse.builder().id("1").build();

        when(notificationHelper.getSubjectCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(eq("subject@test.com"), anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL))).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456")))
            .thenReturn(response);

        anonymityAppliedNotification.sendToSubject(caseData, "1234-5678-9012-3456");

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"));
        assertThat(caseData.getCicCase().getSubjectNotifyList()).isEqualTo(response);
    }

    @Test
    void shouldSendAnonymityAppliedEmailToRepresentative() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeFullName("Rep Name")
                .representativeEmailAddress("rep@test.com")
                .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
                .build())
            .build();

        Map<String, Object> templateVars = new HashMap<>();
        NotificationResponse response = NotificationResponse.builder().id("2").build();

        when(notificationHelper.getRepresentativeCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(eq("rep@test.com"), anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL))).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456")))
            .thenReturn(response);

        anonymityAppliedNotification.sendToRepresentative(caseData, "1234-5678-9012-3456");

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"));
        assertThat(caseData.getCicCase().getRepNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldNotSendSubjectEmailWhenPreferenceIsPost() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().contactPreferenceType(ContactPreferenceType.POST).build())
            .build();

        anonymityAppliedNotification.sendToSubject(caseData, "1234-5678-9012-3456");

        verify(notificationHelper, never()).getSubjectCommonVars(any(), any());
        verify(notificationServiceCIC, never()).sendEmail(any(), any());
    }

    @Test
    void shouldNotSendRepresentativeEmailWhenPreferenceIsPost() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().representativeContactDetailsPreference(ContactPreferenceType.POST).build())
            .build();

        anonymityAppliedNotification.sendToRepresentative(caseData, "1234-5678-9012-3456");

        verify(notificationHelper, never()).getRepresentativeCommonVars(any(), any());
        verify(notificationServiceCIC, never()).sendEmail(any(), any());
    }

    @Test
    void shouldSendNotificationIfNewlyAppliedAndNoBeforeDetails() {
        CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        Map<String, Object> templateVars = new HashMap<>();
        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(eq(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL), any(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL))).thenAnswer(invocation -> NotificationRequest.builder()
            .destinationAddress(invocation.getArgument(0))
            .templateVars(invocation.getArgument(1))
            .template(invocation.getArgument(2))
            .build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456")))
            .thenReturn(NotificationResponse.builder().id("1").build());

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, null, "1234567890");

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"));
    }

    @Test
    void shouldNotSendNotificationIfNotNewlyApplied() {
        CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .build())
            .build();

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, beforeCaseData, "1234567890");
    }
}
