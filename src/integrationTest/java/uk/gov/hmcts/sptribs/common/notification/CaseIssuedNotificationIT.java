package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseIssuedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_BUNDLE_DUE_DATE_TEXT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_RESPONDENT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_POST;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_RESPONDENT_EMAIL_UPDATED;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CaseIssuedNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private CaseIssuedNotification caseIssuedNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(EMAIL)
                .fullName("Subject Name")
                .email("test@email.com")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name"
        );

        caseIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_CITIZEN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendLetterToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(POST)
                .fullName("Subject Name")
                .address(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW"
        );

        caseIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_CITIZEN_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantContactDetailsPreference(EMAIL)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("test@email.com")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Applicant Name"
        );

        caseIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_CITIZEN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendLetterToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantContactDetailsPreference(POST)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Applicant Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW"
        );

        caseIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_CITIZEN_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeContactDetailsPreference(EMAIL)
                .fullName("Subject Name")
                .representativeFullName("Representative Name")
                .representativeEmailAddress("test@email.com")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name"
        );

        caseIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_CITIZEN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendLetterToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeContactDetailsPreference(POST)
                .fullName("Subject Name")
                .representativeFullName("Representative Name")
                .representativeAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW"
        );

        caseIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_CITIZEN_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToRespondent() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .alternativeRespondentEmail("test@email.com")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name",
            CIC_CASE_RESPONDENT_NAME, "Respondent Name",
            CIC_BUNDLE_DUE_DATE_TEXT, "You should provide the tribunal and the Subject/Applicant/Representative "
                + "with a case bundle by 2026-02-12"
        );

        caseIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_RESPONDENT_EMAIL_UPDATED);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
        assertThat(notificationRequest.getUploadedDocuments())
            .isNull();
    }

    @Test
    void shouldSendEmailToRespondentWithDocument() {
        final String documentLabel =
            "[Document 1.pdf A - First decision](http://exui.net/documents/5e32a0d2-9b37-4548-b007-b9b2eb580d0a/binary)";
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(documentLabel)
            .code(UUID.randomUUID())
            .build();
        final Document document = Document.builder()
            .categoryId("A")
            .filename("Document 1.pdf")
            .binaryUrl("http://exui.net/documents/5e32a0d2-9b37-4548-b007-b9b2eb580d0a/binary")
            .url("http://exui.net/documents/5e32a0d2-9b37-4548-b007-b9b2eb580d0a")
            .build();

        final CaseworkerCICDocument cicDocument = CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentEmailContent("Description")
            .documentCategory(DocumentType.EVIDENCE_CORRESPONDENCE_FROM_THE_APPELLANT)
            .build();
        final List<ListValue<CaseworkerCICDocument>> applicantCaseDocuments =
            List.of(ListValue.<CaseworkerCICDocument>builder().value(cicDocument).build());
        final List<CaseworkerCICDocument> selectedDocuments = List.of(cicDocument);

        final List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listItem);

        final DynamicMultiSelectList documentList = new DynamicMultiSelectList();
        documentList.setListItems(listItems);
        documentList.setValue(listItems);

        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .alternativeRespondentEmail("test@email.com")
                .applicantDocumentsUploaded(applicantCaseDocuments)
                .build())
            .caseIssue(CaseIssue.builder()
                .documentList(documentList)
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name",
            CIC_CASE_RESPONDENT_NAME, "Respondent Name",
            CIC_BUNDLE_DUE_DATE_TEXT, "You should provide the tribunal and the Subject/Applicant/Representative "
                + "with a case bundle by 2026-02-12"
        );

        caseIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(selectedDocuments), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_ISSUED_RESPONDENT_EMAIL_UPDATED);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
        assertThat(notificationRequest.getUploadedDocuments())
            .isNotNull();
    }
}
