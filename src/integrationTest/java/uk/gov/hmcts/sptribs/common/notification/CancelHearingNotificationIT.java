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
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.CancelHearingNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

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
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.HEARING_CANCELLED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.HEARING_CANCELLED_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CancelHearingNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private CancelHearingNotification cancelHearingNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToSubject() throws Exception {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(EMAIL)
                .fullName("Subject Name")
                .email("subject@email.com")
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("subject@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00"
            ));
    }

    @Test
    void shouldSendLetterToSubject() throws Exception {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(POST)
                .fullName("Subject Name")
                .address(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00",
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
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("representative@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00"
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
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00",
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
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("respondent@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Respondent Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00"
            ));
    }

    @Test
    void shouldSendEmailToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantContactDetailsPreference(EMAIL)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@email.com")
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("applicant@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00")
            );
    }

    @Test
    void shouldSendLetterToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantContactDetailsPreference(POST)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@email.com")
                .applicantAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .hearingList(
                    DynamicList.builder()
                        .value(
                            DynamicListElement.builder()
                                .label("1 - Final - 21 Apr 2021 10:00")
                                .build()
                        ).build()
                )
                .build())
            .build();

        cancelHearingNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_CANCELLED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                HEARING_DATE, "21 Apr 2021",
                HEARING_TIME, "10:00",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }
}
