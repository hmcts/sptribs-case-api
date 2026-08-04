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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationConstants;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.AnonymityAppliedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.ANONYMITY_APPLIED_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AnonymityAppliedNotificationIT {

    private static final DateTimeFormatter UK_DATE_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.CIC_CASE_UK_DATE_FORMAT);

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private AnonymityAppliedNotification anonymityAppliedNotification;

    @Captor
    private ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToTribunalWithValidDetails() {
        final CaseData data = CaseData.builder()
            .caseStatus(State.AwaitingHearing)
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .anonymisationDate(LocalDate.of(2026, 7, 2))
                .representativeFullName("Rep Name")
                .applicantFullName("Applicant Name")
                .respondentName("Respondent Name")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CONTACT_NAME, CommonConstants.TRIBUNAL_NAME_VALUE,
            CommonConstants.CIC_CASE_HEARING_DATE, "02-07-2026",
            CommonConstants.CIC_CASE_STATUS, "Awaiting Hearing",
            CommonConstants.CIC_CASE_SUBJECT_NAME, "Subject Name",
            CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, "Rep Name",
            CommonConstants.CIC_CASE_APPLICANT_NAME, "Applicant Name",
            CommonConstants.CIC_CASE_RESPONDENT_NAME, "Respondent Name"
        );

        anonymityAppliedNotification.sendToTribunal(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        final NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress()).isEqualTo(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL);
        assertThat(notificationRequest.getTemplate()).isEqualTo(ANONYMITY_APPLIED_EMAIL);
        assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToTribunalWithDefaultValuesWhenFieldsAreNullOrBlank() {
        final CaseData data = CaseData.builder()
            .caseStatus(null)
            .cicCase(CicCase.builder()
                .fullName("")
                .anonymisationDate(null)
                .representativeFullName(" ")
                .applicantFullName(null)
                .respondentName("")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CONTACT_NAME, CommonConstants.TRIBUNAL_NAME_VALUE,
            CommonConstants.CIC_CASE_HEARING_DATE, LocalDate.now().format(UK_DATE_FORMATTER),
            CommonConstants.CIC_CASE_STATUS, CommonConstants.NONE_PROVIDED,
            CommonConstants.CIC_CASE_SUBJECT_NAME, CommonConstants.NONE_PROVIDED,
            CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, CommonConstants.NONE_PROVIDED,
            CommonConstants.CIC_CASE_APPLICANT_NAME, CommonConstants.NONE_PROVIDED,
            CommonConstants.CIC_CASE_RESPONDENT_NAME, CommonConstants.NONE_PROVIDED
        );

        anonymityAppliedNotification.sendToTribunal(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        final NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress()).isEqualTo(NotificationConstants.ANONYMITY_RECIPIENT_EMAIL);
        assertThat(notificationRequest.getTemplate()).isEqualTo(ANONYMITY_APPLIED_EMAIL);
        assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(expectedTemplateVars);
    }
}
