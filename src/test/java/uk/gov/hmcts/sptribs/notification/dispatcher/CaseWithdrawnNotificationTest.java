package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseWithdrawnNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.model.CloseReason.DeathOfAppellant;
import static uk.gov.hmcts.sptribs.caseworker.model.CloseReason.Withdrawn;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DEATH_OF_APPELLANT_EMAIL_CONTENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseWithdrawnNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseWithdrawnNotification caseWithdrawnNotification;

    @Test
    void shouldNotifySubjectCaseWithdrawnWithEmailWithoutAdditionalDetail() {
        final CaseData data = getMockCaseData();
        data.getCloseCase().setCloseCaseReason(DeathOfAppellant);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");
        data.getCloseCase().setAdditionalDetail("");

        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            Map.of(
                CLOSURE_INFORMATION, NONE_PROVIDED,
                CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT),
            TemplateName.CASE_WITHDRAWN_EMAIL);
    }

    @Test
    void shouldNotifySubjectWithEmailCaseWithdrawnWithDeathOfAppellantReason() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");
        data.getCloseCase().setAdditionalDetail("");

        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            Map.of(
                CLOSURE_INFORMATION, NONE_PROVIDED,
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()),
            TemplateName.CASE_WITHDRAWN_EMAIL);
    }

    @Test
    void shouldNotifySubjectCaseWithdrawnWithPost() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());

        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseWithdrawnNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CLOSURE_INFORMATION, data.getCloseCase().getAdditionalDetail(),
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()
            ),
            TemplateName.CASE_WITHDRAWN_POST);
    }

    @Test
    void shouldNotifyRespondentCaseWithdrawnWithEmail() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrespondent@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            Map.of(
                CLOSURE_INFORMATION, data.getCloseCase().getAdditionalDetail(),
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()),
            TemplateName.CASE_WITHDRAWN_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeCaseWithdrawnWithEmail() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            Map.of(
                CLOSURE_INFORMATION, data.getCloseCase().getAdditionalDetail(),
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()),
            TemplateName.CASE_WITHDRAWN_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeCaseWithdrawnWithPost() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseWithdrawnNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CLOSURE_INFORMATION, data.getCloseCase().getAdditionalDetail(),
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()),
            TemplateName.CASE_WITHDRAWN_POST);
    }

    @Test
    void shouldNotifyApplicantCaseWithdrawnWithEmail() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");

        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getApplicantEmailAddress(),
            Map.of(
                CLOSURE_INFORMATION, data.getCloseCase().getAdditionalDetail(),
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()),
            TemplateName.CASE_WITHDRAWN_EMAIL);
    }

    @Test
    void shouldNotifyApplicantCaseWithdrawnWithPost() {
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());

        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseWithdrawnNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CLOSURE_INFORMATION, data.getCloseCase().getAdditionalDetail(),
                CLOSURE_REASON, data.getCloseCase().getCloseCaseReason()),
            TemplateName.CASE_WITHDRAWN_POST);
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();
        CloseCase closeCase = CloseCase.builder()
            .closeCaseReason(Withdrawn)
            .additionalDetail("additionalDet")
            .build();

        return CaseData.builder()
            .cicCase(cicCase)
            .closeCase(closeCase)
            .build();
    }
}
