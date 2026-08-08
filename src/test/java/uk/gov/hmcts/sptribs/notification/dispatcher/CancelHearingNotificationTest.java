package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.testutil.TestEventConstants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CancelHearingNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CancelHearingNotification cancelHearingNotification;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    @Test
    void shouldNotifySubjectOfCaseCancelHearingWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");
        data.getCicCase().setHearingList(getDynamicList());

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
            NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            new ArrayList<>(),
            new HashMap<>(),
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfCaseCancelHearingWithPost() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setHearingList(getDynamicList());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_POST);
    }

    @Test
    void shouldNotifyRespondentOfCaseCancelHearingWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testRespondent@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
            NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            new ArrayList<>(),
            new HashMap<>(),
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseCancelHearingWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setHearingList(getDynamicList());

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
            NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            eq(Party.REPRESENTATIVE));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            new ArrayList<>(),
            new HashMap<>(),
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseCancelHearingWithPost() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setHearingList(getDynamicList());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_POST);
    }


    @Test
    void shouldNotifyApplicantOfCaseCancelHearingWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setApplicantFullName("applicantFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testapplicant@outlook.com");
        data.getCicCase().setHearingList(getDynamicList());

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
            NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getApplicantEmailAddress(),
            new ArrayList<>(),
            new HashMap<>(),
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_EMAIL);
    }

    @Test
    void shouldNotifyApplicantOfCaseCancelHearingWithPost() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setApplicantFullName("applicantFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setHearingList(getDynamicList());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(NOTIFICATION_RESPONSE);

        cancelHearingNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.HEARING_CANCELLED_POST);
    }


    private CaseData getMockCaseData(LocalDate stayCaseExpDate) {
        final CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();
        final CaseStay caseStay = CaseStay.builder()
            .expirationDate(stayCaseExpDate)
            .stayReason(StayReason.OTHER)
            .additionalDetail("addlDetail")
            .build();

        return CaseData.builder()
            .cicCase(cicCase)
            .caseStay(caseStay)
            .build();
    }

    private DynamicList getDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("HearingType" + SPACE + HYPHEN + SPACE + HEARING_DATE_1 + TestEventConstants.SPACE + HEARING_TIME)
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
