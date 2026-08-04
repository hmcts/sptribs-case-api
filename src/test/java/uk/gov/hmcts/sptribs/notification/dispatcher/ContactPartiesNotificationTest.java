package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ContactPartiesNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ContactPartiesNotification contactPartiesNotification;

    private static final int DOC_ATTACH_LIMIT = 10;

    private CaseData caseData;
    private CicCase cicCase;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        cicCase = CicCase.builder()
            .fullName("fullName")
            .caseNumber(TEST_CASE_ID.toString())
            .notifyPartyMessage("message")
            .build();

        caseData.setCicCase(cicCase);
        caseData.setContactPartiesDocuments(ContactPartiesDocuments
            .builder()
            .build());

        List<ListValue<CaseworkerCICDocument>> docs = TestDataHelper.getCaseworkerCICDocumentList("test.pdf", "test1.doc", "test2.pdf");
        caseData.setContactPartiesDocuments(ContactPartiesDocuments
            .builder()
            .build());
        caseData.setAllDocManagement(DocumentManagement
            .builder()
            .caseworkerCICDocument(docs)
            .build());

        DynamicMultiSelectList contactPartiesList = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "test.url");
        contactPartiesList.setValue(contactPartiesList.getListItems());
        caseData
            .getContactPartiesDocuments()
            .setDocumentList(contactPartiesList);
    }

    @Test
    void shouldNotifySubjectByEmailWithCorrectPartyAndWriteBack() {
        //given
        cicCase.setContactPreferenceType(ContactPreferenceType.EMAIL);
        cicCase.setEmail("subject@example.com");

        Map<String, Object> baseVars = new HashMap<>();
        Map<String, String> uploadedDocuments = Map.of("doc1", "content");
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-id-123");

        when(notificationHelper.getSubjectCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(uploadedDocuments);
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT)))
            .thenReturn(response);

        //when
        String correspondenceId = contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(correspondenceId).isEqualTo("resp-id-123");
        verify(notificationService).sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq("subject@example.com"),
            eq(true),
            eq(uploadedDocuments),
            argThat(vars -> "message".equals(vars.get(CommonConstants.CONTACT_PARTY_INFO))
                && "fullName".equals(vars.get(CommonConstants.CIC_CASE_SUBJECT_NAME))),
            eq(TemplateName.CONTACT_PARTIES_EMAIL)
        );
        assertThat(cicCase.getSubjectNotifyList()).isEqualTo(response);
    }

    @Test
    void shouldNotifySubjectByLetterWhenPreferenceIsNotEmail() {
        //given
        cicCase.setContactPreferenceType(ContactPreferenceType.POST);
        AddressGlobalUK address = AddressGlobalUK
            .builder()
            .addressLine1("1 Test St")
            .build();
        cicCase.setAddress(address);

        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest letterRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("letter-resp-id");

        when(notificationHelper.getSubjectCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), eq(CONTACT_PARTIES_POST)))
            .thenReturn(letterRequest);
        when(notificationService.sendLetter(letterRequest, TEST_CASE_ID.toString())).thenReturn(response);

        //when
        String correspondenceId = contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(correspondenceId).isEqualTo("letter-resp-id");
        verify(notificationHelper).addAddressTemplateVars(eq(address), anyMap());
        verify(notificationService, never()).sendEmail(any(), any(), any(), any());
        assertThat(cicCase.getSubjectLetterNotifyList()).isEqualTo(response);
    }

    @Test
    void shouldNotifyApplicantByEmailWithCorrectPartyAndWriteBack() {
        //given
        cicCase.setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        cicCase.setApplicantEmailAddress("applicant@example.com");

        Map<String, Object> baseVars = new HashMap<>();
        Map<String, String> uploadedDocuments = Map.of("doc1", "content");
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-applicant-id");

        when(notificationHelper.getApplicantCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(uploadedDocuments);
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT)))
            .thenReturn(response);

        //when
        String correspondenceId = contactPartiesNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(correspondenceId).isEqualTo("resp-applicant-id");
        verify(notificationService).sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq("applicant@example.com"),
            eq(true),
            eq(uploadedDocuments),
            argThat(vars -> "message".equals(vars.get(CommonConstants.CONTACT_PARTY_INFO))
                && "fullName".equals(vars.get(CommonConstants.CIC_CASE_SUBJECT_NAME))),
            eq(TemplateName.CONTACT_PARTIES_EMAIL)
        );
        assertThat(cicCase.getAppNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldNotifyApplicantByLetterWhenPreferenceIsNotEmail() {
        //given
        cicCase.setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        AddressGlobalUK address = AddressGlobalUK.builder().addressLine1("1 Applicant St").build();
        cicCase.setApplicantAddress(address);

        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest letterRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("letter-applicant-id");

        when(notificationHelper.getApplicantCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), eq(CONTACT_PARTIES_POST)))
            .thenReturn(letterRequest);
        when(notificationService.sendLetter(letterRequest, TEST_CASE_ID.toString())).thenReturn(response);

        //when
        String correspondenceId = contactPartiesNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(correspondenceId).isEqualTo("letter-applicant-id");
        verify(notificationHelper).addAddressTemplateVars(eq(address), anyMap());
        verify(notificationService, never()).sendEmail(any(), any(), any(), any());
        assertThat(cicCase.getAppLetterNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldNotifyRepresentativeByEmailWithCorrectPartyAndWriteBack() {
        //given
        cicCase.setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        cicCase.setRepresentativeEmailAddress("representative@example.com");

        Map<String, Object> baseVars = new HashMap<>();
        Map<String, String> uploadedDocuments = Map.of("doc1", "content");
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-representative-id");

        when(notificationHelper.getRepresentativeCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(uploadedDocuments);
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE)))
            .thenReturn(response);

        //when
        String correspondenceId = contactPartiesNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(correspondenceId).isEqualTo("resp-representative-id");
        verify(notificationService).sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq("representative@example.com"),
            eq(true),
            eq(uploadedDocuments),
            argThat(vars -> "message".equals(vars.get(CommonConstants.CONTACT_PARTY_INFO))
                && "fullName".equals(vars.get(CommonConstants.CIC_CASE_SUBJECT_NAME))),
            eq(TemplateName.CONTACT_PARTIES_EMAIL)
        );
        assertThat(cicCase.getRepNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldNotifyRepresentativeByLetterWhenPreferenceIsNotEmail() {
        //given
        cicCase.setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        AddressGlobalUK address = AddressGlobalUK.builder().addressLine1("1 Representative St").build();
        cicCase.setRepresentativeAddress(address);

        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest letterRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("letter-representative-id");

        when(notificationHelper.getRepresentativeCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), eq(CONTACT_PARTIES_POST)))
            .thenReturn(letterRequest);
        when(notificationService.sendLetter(letterRequest, TEST_CASE_ID.toString())).thenReturn(response);

        //when
        String correspondenceId = contactPartiesNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(correspondenceId).isEqualTo("letter-representative-id");
        verify(notificationHelper).addAddressTemplateVars(eq(address), anyMap());
        verify(notificationService, never()).sendEmail(any(), any(), any(), any());
        assertThat(cicCase.getRepLetterNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldNotifyRespondentByEmailOnlyRegardlessOfPreference() {
        //given
        cicCase.setRespondentEmail("respondent@example.com");
        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-respondent-id");

        when(notificationHelper.getRespondentCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(Map.of());
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT)))
            .thenReturn(response);

        //when
        contactPartiesNotification.sendToRespondent(caseData, TEST_CASE_ID.toString());

        //then
        verify(notificationService).sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
        assertThat(cicCase.getResNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldNotifyTribunalAndWriteToTribunalField() {
        //given
        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-tribunal-id");

        when(notificationHelper.getTribunalCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(Map.of());
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL)))
            .thenReturn(response);

        //when
        contactPartiesNotification.sendToTribunal(caseData, TEST_CASE_ID.toString());

        //then
        assertThat(cicCase.getTribunalNotificationResponse()).isEqualTo(response);
    }

    @Test
    void shouldUseCitizenDashboardTemplateWithLink_subject() {
        //given
        ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardEnabled", true);
        ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");

        cicCase.setContactPreferenceType(ContactPreferenceType.EMAIL);
        cicCase.setEmail("subject@example.com");

        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-id");

        when(notificationHelper.getSubjectCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(Map.of());
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT)))
            .thenReturn(response);

        //when
        contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

        //then
        verify(notificationHelper).buildEmailNotificationRequest(
            any(), anyBoolean(), anyMap(),
            argThat(vars -> "https://frontend.url/dashboard".equals(vars.get(CommonConstants.DASHBOARD_KEY))),
            eq(TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD)
        );
    }


    @Test
    void shouldNotDashboardLinkWhenCitizenDashboardDisabled() {
        //given
        ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardEnabled", false);
        ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");

        cicCase.setContactPreferenceType(ContactPreferenceType.EMAIL);
        cicCase.setEmail("subject@example.com");

        Map<String, Object> baseVars = new HashMap<>();
        NotificationRequest builtRequest = mock(NotificationRequest.class);
        NotificationResponse response = new NotificationResponse();
        response.setId("resp-id");

        when(notificationHelper.getSubjectCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(baseVars);
        when(notificationHelper.buildDocumentList(any(), eq(DOC_ATTACH_LIMIT))).thenReturn(Map.of());
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(builtRequest);
        when(notificationService.sendEmail(eq(builtRequest), anyList(), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT)))
            .thenReturn(response);

        //when
        contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

        //then
        verify(notificationHelper).buildEmailNotificationRequest(
            any(), anyBoolean(), anyMap(),
            argThat(vars -> !vars.containsKey(CommonConstants.DASHBOARD_KEY)),
            eq(TemplateName.CONTACT_PARTIES_EMAIL)
        );
    }
}
