package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseIssuedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseIssuedNotification caseIssuedNotification;

    @Test
    void shouldNotifySubjectOfCaseIssuedCitizenWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("TestSubject@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            Map.of(CommonConstants.CIC_CASE_SUBJECT_NAME,data.getCicCase().getFullName()),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfCaseIssuedCitizenWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName()),
            TemplateName.CASE_ISSUED_CITIZEN_POST);
    }

    @Test
    void shouldNotifyApplicantOfCaseIssuedCitizenWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("TestApplicant@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getApplicantEmailAddress(),
            Map.of(CommonConstants.CIC_CASE_APPLICANT_NAME,data.getCicCase().getApplicantFullName()),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
    }

    @Test
    void shouldNotifyApplicantOfCaseIssuedCitizenWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(CommonConstants.CIC_CASE_APPLICANT_NAME,data.getCicCase().getApplicantFullName()),
            TemplateName.CASE_ISSUED_CITIZEN_POST);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseIssuedCitizenWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeEmailAddress("TestRepresentative@outlook.com");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            Map.of(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME,data.getCicCase().getRepresentativeFullName()),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseIssuedCitizenWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME,data.getCicCase().getRepresentativeFullName()),
            TemplateName.CASE_ISSUED_CITIZEN_POST);
    }

    @Test
    void shouldNotifyRespondentOfCaseIssuedCitizenWithEmailWithoutAttachments() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setRespondentEmail("testRespondentEmail@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getAlternativeRespondentEmail(),
            Map.of(CommonConstants.CIC_CASE_RESPONDENT_NAME,data.getCicCase().getRespondentName()),
            TemplateName.CASE_ISSUED_RESPONDENT_EMAIL);
    }

    @Test
    void shouldNotifyRespondentOfCaseIssuedCitizenWithEmailWithAttachments() {
        final Document document = Document.builder()
            .filename("test file")
            .url("test.url/documentId")
            .binaryUrl("test.url/documentId/binary")
            .build();
        final CaseworkerCICDocument cicDocument = CaseworkerCICDocument.builder()
            .date(LocalDate.of(2025, 12, 11))
            .documentCategory(DocumentType.APPLICATION_FOR_AN_EXTENSION_OF_TIME)
            .documentEmailContent("description")
            .documentLink(document)
            .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocuments =
            List.of(ListValue.<CaseworkerCICDocument>builder().value(cicDocument).build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
                .data(getMockCaseData())
                .build();
        final CaseData data = caseDetails.getData();
        final CicCase cicCase = data.getCicCase();
        cicCase.setApplicantDocumentsUploaded(applicantDocuments);

        DynamicMultiSelectList dynamicMultiSelectList = DocumentListUtil.prepareDocumentList(data, "test.url");
        dynamicMultiSelectList.setValue(dynamicMultiSelectList.getListItems());
        DocumentListUtil.getAllCaseDocuments(data);
        final CaseIssue caseIssue = CaseIssue.builder()
                .documentList(dynamicMultiSelectList)
                .build();

        data.setCaseIssue(caseIssue);
        data.getCicCase().setRepresentativeFullName("respFullName");

        //When
        when(notificationHelper.buildDocumentList(caseIssue.getDocumentList(), 5))
            .thenReturn(getDocumentUploadMap());
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        caseIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(List.of(cicDocument)), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getAlternativeRespondentEmail(),
            true,
            getDocumentUploadMap(),
            Map.of(CommonConstants.CIC_CASE_RESPONDENT_NAME,data.getCicCase().getRespondentName()),
            TemplateName.CASE_ISSUED_RESPONDENT_EMAIL);

    }

    private CaseData getMockCaseData() {
        final CicCase cicCase = CicCase.builder()
            .fullName("fullName")
            .caseNumber(TEST_CASE_ID.toString())
            .build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }

    private Map<String, String> getDocumentUploadMap() {
        return Map.of(
            "CaseDocument1", "documentId",
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
