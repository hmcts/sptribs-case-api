package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseWithdrawnNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.caseworker.model.CloseReason.DeathOfAppellant;
import static uk.gov.hmcts.sptribs.caseworker.model.CloseReason.Rule27;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DEATH_OF_APPELLANT_EMAIL_CONTENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_WITHDRAWN_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_WITHDRAWN_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import uk.gov.hmcts.sptribs.testutil.IntegrationTest;

@ExtendWith(SpringExtension.class)
@IntegrationTest
public class CaseWithdrawnNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private CaseWithdrawnNotification caseWithdrawnNotification;

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
            .closeCase(CloseCase.builder()
                .closeCaseReason(DeathOfAppellant)
                .additionalDetail("some closure detail")
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("subject@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT,
                CLOSURE_INFORMATION, "some closure detail"
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
            .closeCase(CloseCase.builder()
                .closeCaseReason(DeathOfAppellant)
                .additionalDetail("some closure detail")
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT,
                CLOSURE_INFORMATION, "some closure detail",
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
            .closeCase(CloseCase.builder()
                .closeCaseReason(Rule27)
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("representative@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                CLOSURE_REASON, Rule27,
                CLOSURE_INFORMATION, NONE_PROVIDED
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
            .closeCase(CloseCase.builder()
                .closeCaseReason(DeathOfAppellant)
                .additionalDetail("some closure detail")
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT,
                CLOSURE_INFORMATION, "some closure detail",
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
            .closeCase(CloseCase.builder()
                .closeCaseReason(Rule27)
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("respondent@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Respondent Name",
                CLOSURE_REASON, Rule27,
                CLOSURE_INFORMATION, NONE_PROVIDED
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
            .closeCase(CloseCase.builder()
                .closeCaseReason(DeathOfAppellant)
                .additionalDetail("some closure detail")
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("applicant@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT,
                CLOSURE_INFORMATION, "some closure detail"
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
            .closeCase(CloseCase.builder()
                .closeCaseReason(DeathOfAppellant)
                .additionalDetail("some closure detail")
                .build()
            )
            .build();

        caseWithdrawnNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CASE_WITHDRAWN_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT,
                CLOSURE_INFORMATION, "some closure detail",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }
}
