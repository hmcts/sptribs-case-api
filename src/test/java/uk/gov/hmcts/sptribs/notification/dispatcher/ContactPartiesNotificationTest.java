package uk.gov.hmcts.sptribs.notification.dispatcher;

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
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDynamicMultiSelectDocumentList;

@ExtendWith(MockitoExtension.class)
class ContactPartiesNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ContactPartiesNotification contactPartiesNotification;

    private static final UUID TEST_DOCUMENT_ID = UUID.randomUUID();

    @Test
    void shouldNotifySubjectOfContactPartiesWithEmailWithAttachments() {
        //Given
        final CaseData data = getMockCaseData();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().uploadedDocuments(uploadedDocuments).build());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            true,
            uploadedDocuments,
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfContactPartiesWithPost() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString())))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_POST);
    }

    @Test
    void shouldNotifyApplicantOfContactPartiesWithEmailWithAttachments() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().uploadedDocuments(getDocumentUploadMap()).build());

        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getApplicantEmailAddress(),
            true,
            uploadedDocuments,
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyApplicantOfContactPartiesWithPost() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString())))
            .thenReturn(notificationResponse);
        String correspondenceId =
            contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_POST);
    }

    @Test
    void shouldNotifyRepresentativeOfContactPartiesWithEmailWithAttachments() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        final ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().uploadedDocuments(getDocumentUploadMap()).build());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            true,
            uploadedDocuments,
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfContactPartiesWithPost() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString())))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_POST);
    }


    @Test
    void shouldNotifyRespondentOfContactPartiesWithEmail() {
        //Given
        final  Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyRespondentOfContactPartiesWithEmailWithAttachments() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().uploadedDocuments(getDocumentUploadMap()).build());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            true,
            uploadedDocuments,
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyTribunalOfContactPartiesWithEmail() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setNotifyPartyMessage("message");
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString(), uploadedDocuments);
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL));
        verify(notificationHelper).buildEmailNotificationRequest(
            TRIBUNAL_EMAIL_VALUE,
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    @Test
    void shouldNotifyTribunalOfContactPartiesWithEmailWithAttachments() {
        //Given
        final Map<String, String> uploadedDocuments = getDocumentUploadMap();
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setNotifyPartyMessage("message");
        final Map<String, Object> comonVarsMap = Map.of(
            CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
            CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId("123");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().uploadedDocuments(getDocumentUploadMap()).build());

        when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL)))
            .thenReturn(notificationResponse);

        String correspondenceId =
            contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString(), uploadedDocuments);

        //Then
        assertThat(correspondenceId).isEqualTo("123");
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL));
        verify(notificationHelper).buildEmailNotificationRequest(
            TRIBUNAL_EMAIL_VALUE,
            true,
            uploadedDocuments,
            comonVarsMap,
            TemplateName.CONTACT_PARTIES_EMAIL);
    }

    private CaseData getMockCaseData() {
        final CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }

    private java.util.Map<String, String> getDocumentUploadMap() {
        return java.util.Map.of(
            "CaseDocument1", TEST_DOCUMENT_ID.toString(),
            "CaseDocument2", "",
            "CaseDocument3", "",
            "CaseDocument4", "",
            "CaseDocument5", "",
            "DocumentAvailable1", "yes",
            "DocumentAvailable2", "no",
            "DocumentAvailable3", "no",
            "DocumentAvailable4", "no",
            "DocumentAvailable5", "no");
    }
}
