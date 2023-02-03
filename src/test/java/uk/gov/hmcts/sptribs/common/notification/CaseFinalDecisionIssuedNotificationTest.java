package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.document.CaseDocumentClient;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class CaseFinalDecisionIssuedNotificationTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @Mock
    private Resource resource;

    @InjectMocks
    private CaseFinalDecisionIssuedNotification finalDecisionIssuedNotification;

    @Test
    void shouldNotifySubjectWithEmail() throws IOException {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document guidanceDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().documents(List.of(documentListValue))
            .finalDecisionGuidance(guidanceDocument).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(firstFile));
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService, times(2)).getJsonFileAttachment(any());
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithEmailWithNoUploadedDocument() {
        //Given
        LocalDate expDate = LocalDate.now();
        final User systemUser = mock(User.class);
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().finalDecisionDraft(document).finalDecisionGuidance(document).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(null));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithEmailThrowsException() throws IOException {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().finalDecisionDraft(document).finalDecisionGuidance(document).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenThrow(IOException.class);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithPost() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendLetter();
    }

    @Test
    void shouldNotifyRespondentWithEmail() throws IOException {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRespondantName("respondentName");
        data.getCicCase().setRespondantEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().finalDecisionDraft(document).finalDecisionGuidance(document).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(firstFile));
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        finalDecisionIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRespondentWithEmailWithException() throws IOException {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRespondantName("respondentName");
        data.getCicCase().setRespondantEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().finalDecisionDraft(document).finalDecisionGuidance(document).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenThrow(IOException.class);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        finalDecisionIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithEmail() throws IOException {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().finalDecisionDraft(document).finalDecisionGuidance(document).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(firstFile));
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithEmailWithException() throws IOException {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().finalDecisionDraft(document).finalDecisionGuidance(document).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenThrow(IOException.class);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithPost() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendLetter();
    }

    private CaseData getMockCaseData(LocalDate stayCaseExpDate) {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        return caseData;
    }
}
