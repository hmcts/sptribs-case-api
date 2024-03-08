package uk.gov.hmcts.sptribs.common.notification;

import org.elasticsearch.core.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseStayedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseStayedNotification caseStayedNotification;

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseStayedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testSubject@outlook.com",
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, data.getCaseStay().getAdditionalDetail(),
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmailWithoutAdditionalDetail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");
        data.getCaseStay().setAdditionalDetail("");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseStayedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testSubject@outlook.com",
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, CommonConstants.NONE_PROVIDED,
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithPost() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseStayedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, data.getCaseStay().getAdditionalDetail(),
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_POST);
    }

    @Test
    void shouldNotifyApplicantOfApplicationReceivedWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseStayedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testApplicant@outlook.com",
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, data.getCaseStay().getAdditionalDetail(),
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_EMAIL);
    }

    @Test
    void shouldNotifyApplicantOfApplicationReceivedWithPost() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseStayedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, data.getCaseStay().getAdditionalDetail(),
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_POST);
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseStayedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, data.getCaseStay().getAdditionalDetail(),
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithPost() {
        //Given
        final LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseStayedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CommonConstants.STAY_ADDITIONAL_DETAIL, data.getCaseStay().getAdditionalDetail(),
                CommonConstants.STAY_EXPIRATION_DATE, data.getCaseStay().getExpirationDate(),
                CommonConstants.STAY_REASON, data.getCaseStay().getStayReason().getType()
            ),
            TemplateName.CASE_STAYED_POST);
    }

    private CaseData getMockCaseData(LocalDate stayCaseExpDate) {
        final CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
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

}
