package uk.gov.hmcts.sptribs.common.notification;

import org.elasticsearch.core.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDynamicMultiSelectDocumentList;

@ExtendWith(MockitoExtension.class)
class ContactPartiesNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ContactPartiesNotification contactPartiesNotification;

    @Test
    void shouldNotifySubjectOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testSubject@outlook.com",
            true,
            new HashMap<>(),
            Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName()
            ),
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfCaseIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName(),
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage()
            ),
            TemplateName.CONTACT_PARTIES_POST);
    }

    @Test
    void shouldNotifyApplicantOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testApplicant@outlook.com",
            true,
            new HashMap<>(),
            Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName()
            ),
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyApplicantOfCaseIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName(),
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage()
            ),
            TemplateName.CONTACT_PARTIES_POST);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testrepr@outlook.com",
            true,
            new HashMap<>(),
            Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName()
            ),
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName(),
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage()
            ),
            TemplateName.CONTACT_PARTIES_POST);
    }

    @Test
    void shouldNotifyRespondentOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setNotifyPartyMessage("message");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName()
            ),
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyRespondentOfCaseIssuedWithEmailWithAttachments() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setNotifyPartyMessage("message");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            true,
            new HashMap<>(),
            Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName()
            ),
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyTribunalOfCaseIssuedWithEmailWithAttachments() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setNotifyPartyMessage("message");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToTribunal(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            TRIBUNAL_EMAIL_VALUE,
            true,
            new HashMap<>(),
            Map.of(
                CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE,
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage()
            ),
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    private CaseData getMockCaseData() {
        final CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber("CN1").build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
