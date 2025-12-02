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
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.DecisionIssuedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.caseworker.model.NoticeOption.UPLOAD_FROM_COMPUTER;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DecisionIssuedNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private DecisionIssuedNotification decisionIssuedNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(EMAIL)
                .fullName("Subject Name")
                .email("subject@email.com")
                .build())
            .caseIssueDecision(CaseIssueDecision.builder()
                .decisionNotice(UPLOAD_FROM_COMPUTER)
                .decisionDocument(CICDocument.builder()
                    .documentLink(Document.builder()
                        .url("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982")
                        .filename("DRAFT :Order--[null]--09-05-2024 15:11:08.pdf")
                        .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982/binary")
                        .categoryId("TD")
                        .build())
                    .build())
                .build()
            )
            .build();

        decisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("subject@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                DECISION_NOTICE + 1, "0ebad3a7-223e-4185-b8ce-ccb50a87e982",
                DOC_AVAILABLE + 2, "no",
                DECISION_NOTICE + 2, EMPTY_PLACEHOLDER
            ));
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

        decisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }

    @Test
    void shouldSendEmailToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .representativeContactDetailsPreference(EMAIL)
                .representativeFullName("Representative Name")
                .representativeEmailAddress("representative@email.com")
                .build())
            .caseIssueDecision(CaseIssueDecision.builder()
                .decisionNotice(UPLOAD_FROM_COMPUTER)
                .decisionDocument(CICDocument.builder()
                    .documentLink(Document.builder()
                        .url("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982")
                        .filename("DRAFT :Order--[null]--09-05-2024 15:11:08.pdf")
                        .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982/binary")
                        .categoryId("TD")
                        .build())
                    .build())
                .build()
            )
            .build();

        decisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("representative@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                DECISION_NOTICE + 1, "0ebad3a7-223e-4185-b8ce-ccb50a87e982",
                DOC_AVAILABLE + 2, "no",
                DECISION_NOTICE + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendLetterToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .representativeContactDetailsPreference(POST)
                .representativeFullName("Representative Name")
                .representativeEmailAddress("representative@email.com")
                .representativeAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .build();

        decisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }

    @Test
    void shouldSendEmailToRespondent() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@email.com")
                .build())
            .caseIssueDecision(CaseIssueDecision.builder()
                .decisionNotice(UPLOAD_FROM_COMPUTER)
                .decisionDocument(CICDocument.builder()
                    .documentLink(Document.builder()
                        .url("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982")
                        .filename("DRAFT :Order--[null]--09-05-2024 15:11:08.pdf")
                        .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982/binary")
                        .categoryId("TD")
                        .build())
                    .build())
                .build()
            )
            .build();

        decisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("respondent@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Respondent Name"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                DECISION_NOTICE + 1, "0ebad3a7-223e-4185-b8ce-ccb50a87e982",
                DOC_AVAILABLE + 2, "no",
                DECISION_NOTICE + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendEmailToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(EMAIL)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@email.com")
                .build())
            .caseIssueDecision(CaseIssueDecision.builder()
                .decisionNotice(UPLOAD_FROM_COMPUTER)
                .decisionDocument(CICDocument.builder()
                    .documentLink(Document.builder()
                        .url("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982")
                        .filename("DRAFT :Order--[null]--09-05-2024 15:11:08.pdf")
                        .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/0ebad3a7-223e-4185-b8ce-ccb50a87e982/binary")
                        .categoryId("TD")
                        .build())
                    .build())
                .build()
            )
            .build();

        decisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("applicant@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                DECISION_NOTICE + 1, "0ebad3a7-223e-4185-b8ce-ccb50a87e982",
                DOC_AVAILABLE + 2, "no",
                DECISION_NOTICE + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendLetterToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(POST)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@email.com")
                .applicantAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .build();

        decisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(DECISION_ISSUED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }
}
