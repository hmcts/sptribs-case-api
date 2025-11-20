package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseStayedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_ADDITIONAL_DETAIL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_EXPIRATION_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_STAYED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_STAYED_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import uk.gov.hmcts.sptribs.testutil.IntegrationTest;

@ExtendWith(SpringExtension.class)
@IntegrationTest
public class CaseStayedNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private CaseStayedNotification caseStayedNotification;

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
            .caseStay(CaseStay.builder()
                .expirationDate(LocalDate.of(2024, 3, 1))
                .stayReason(StayReason.OTHER)
                .additionalDetail("Additional Detail")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name",
            STAY_EXPIRATION_DATE, LocalDate.of(2024, 3, 1),
            STAY_REASON, "Other",
            STAY_ADDITIONAL_DETAIL, "Additional Detail"
        );

        caseStayedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_STAYED_EMAIL);
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
            .caseStay(CaseStay.builder()
                .expirationDate(LocalDate.of(2024, 3, 1))
                .stayReason(StayReason.OTHER)
                .additionalDetail("Additional Detail")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW",
            STAY_EXPIRATION_DATE, LocalDate.of(2024, 3, 1),
            STAY_REASON, "Other",
            STAY_ADDITIONAL_DETAIL, "Additional Detail"
        );

        caseStayedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_STAYED_POST);
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
            .caseStay(CaseStay.builder()
                .expirationDate(LocalDate.of(2024, 3, 1))
                .stayReason(StayReason.OTHER)
                .additionalDetail("Additional Detail")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Applicant Name",
            STAY_EXPIRATION_DATE, LocalDate.of(2024, 3, 1),
            STAY_REASON, "Other",
            STAY_ADDITIONAL_DETAIL, "Additional Detail"
        );

        caseStayedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_STAYED_EMAIL);
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
            .caseStay(CaseStay.builder()
                .expirationDate(LocalDate.of(2024, 3, 1))
                .stayReason(StayReason.OTHER)
                .additionalDetail("Additional Detail")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Applicant Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW",
            STAY_EXPIRATION_DATE, LocalDate.of(2024, 3, 1),
            STAY_REASON, "Other",
            STAY_ADDITIONAL_DETAIL, "Additional Detail"
        );

        caseStayedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_STAYED_POST);
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
            .caseStay(CaseStay.builder()
                .expirationDate(LocalDate.of(2024, 3, 1))
                .stayReason(StayReason.OTHER)
                .additionalDetail("Additional Detail")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name",
            STAY_EXPIRATION_DATE, LocalDate.of(2024, 3, 1),
            STAY_REASON, "Other",
            STAY_ADDITIONAL_DETAIL, "Additional Detail"
        );

        caseStayedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_STAYED_EMAIL);
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
            .caseStay(CaseStay.builder()
                .expirationDate(LocalDate.of(2024, 3, 1))
                .stayReason(StayReason.OTHER)
                .additionalDetail("Additional Detail")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW",
            STAY_EXPIRATION_DATE, LocalDate.of(2024, 3, 1),
            STAY_REASON, "Other",
            STAY_ADDITIONAL_DETAIL, "Additional Detail"
        );

        caseStayedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_STAYED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }
}
