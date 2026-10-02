package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter UK_DATE_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.CIC_CASE_UK_DATE_FORMAT);

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
    void shouldSendAnonymityAppliedEmailToTribunal() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .cicCase(CicCase.builder()
                .anonymisationDate(LocalDate.of(2026, 7, 2))
                .fullName("Test Subject")
                .representativeFullName("Rep Name")
                .applicantFullName("Applicant Name")
                .respondentName("Respondent Name")
                .build())
            .build();

        final Map<String, Object> templateVars = new HashMap<>();
        final NotificationResponse response = NotificationResponse.builder().id("1").build();

        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(
            eq(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL),
            anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL)
        )).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null)))
            .thenReturn(response);

        anonymityAppliedNotification.sendToTribunal(caseData, "1234-5678-9012-3456");

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null));
        assertThat(caseData.getCicCase().getTribunalNotificationResponse()).isEqualTo(response);
        assertThat(templateVars)
            .containsEntry(CommonConstants.CIC_CASE_HEARING_DATE, "02-07-2026")
            .containsEntry(CommonConstants.CIC_CASE_STATUS, "Awaiting Hearing")
            .containsEntry(CommonConstants.CIC_CASE_SUBJECT_NAME, "Test Subject")
            .containsEntry(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, "Rep Name")
            .containsEntry(CommonConstants.CIC_CASE_APPLICANT_NAME, "Applicant Name")
            .containsEntry(CommonConstants.CIC_CASE_RESPONDENT_NAME, "Respondent Name");
    }

    @Test
    void shouldSendAnonymityAppliedEmailToTribunalWithDefaultValuesWhenFieldsAreNullOrBlank() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(null)
            .cicCase(CicCase.builder()
                .anonymisationDate(null)
                .fullName("")
                .representativeFullName(" ")
                .applicantFullName(null)
                .respondentName("")
                .build())
            .build();

        final Map<String, Object> templateVars = new HashMap<>();
        final NotificationResponse response = NotificationResponse.builder().id("2").build();

        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(
            eq(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL),
            anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL)
        )).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null)))
            .thenReturn(response);

        anonymityAppliedNotification.sendToTribunal(caseData, "1234-5678-9012-3456");

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null));
        assertThat(caseData.getCicCase().getTribunalNotificationResponse()).isEqualTo(response);
        assertThat(templateVars)
            .containsEntry(CommonConstants.CIC_CASE_HEARING_DATE, LocalDate.now().format(UK_DATE_FORMATTER))
            .containsEntry(CommonConstants.CIC_CASE_STATUS, CommonConstants.NONE_PROVIDED)
            .containsEntry(CommonConstants.CIC_CASE_SUBJECT_NAME, CommonConstants.NONE_PROVIDED)
            .containsEntry(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, CommonConstants.NONE_PROVIDED)
            .containsEntry(CommonConstants.CIC_CASE_APPLICANT_NAME, CommonConstants.NONE_PROVIDED)
            .containsEntry(CommonConstants.CIC_CASE_RESPONDENT_NAME, CommonConstants.NONE_PROVIDED);
    }

    @Test
    void shouldSendNotificationIfNewlyAppliedAndNoBeforeDetails() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        final Map<String, Object> templateVars = new HashMap<>();
        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(
            eq(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL),
            anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL)
        )).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null)))
            .thenReturn(NotificationResponse.builder().id("1").build());

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, null);

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null));
    }

    @Test
    void shouldSendNotificationIfNewlyAppliedAndBeforeDetailsExistsButAnonymityWasNotApplied() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        final CaseData beforeData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .anonymityAlreadyApplied(YesOrNo.NO)
                .build())
            .build();

        final Map<String, Object> templateVars = new HashMap<>();
        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(
            eq(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL),
            anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL)
        )).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null)))
            .thenReturn(NotificationResponse.builder().id("1").build());

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, beforeData);

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"), eq(null));
    }

    @Test
    void shouldNotSendNotificationIfNotNewlyApplied() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        final CaseData beforeCaseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .build())
            .build();

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, beforeCaseData);

        verify(notificationServiceCIC, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationIfAnonymiseYesOrNoIsNotYes() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, null);

        verify(notificationServiceCIC, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationIfAnonymisedAppellantNameIsNull() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName(null)
                .fullName("Test Subject")
                .build())
            .build();

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, null);

        verify(notificationServiceCIC, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationIfCaseDataIsNull() {
        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(null, null);

        verify(notificationServiceCIC, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationIfCicCaseIsNull() {
        final CaseData caseData = CaseData.builder()
            .cicCase(null)
            .build();

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, null);

        verify(notificationServiceCIC, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldPropagateExceptionWhenSendToTribunalThrows() {
        final CaseData caseData = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Subject")
                .build())
            .build();

        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData)))
            .thenThrow(new RuntimeException("Service failure"));

        // Should propagate the exception when sendToTribunal throws
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
            anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(caseData, null)
        );

        verify(notificationServiceCIC, never()).sendEmail(any(), any(), any());
    }
}
