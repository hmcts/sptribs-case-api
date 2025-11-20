package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.DssUpdateCaseSubmissionNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import uk.gov.hmcts.sptribs.testutil.IntegrationTest;

@ExtendWith(SpringExtension.class)
@IntegrationTest
public class DssUpdateCaseSubmissionNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private DssUpdateCaseSubmissionNotification dssUpdateCaseSubmissionNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToApplicant() {
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

        dssUpdateCaseSubmissionNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(UPDATE_RECEIVED);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToTribunal() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .build())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, TRIBUNAL_NAME_VALUE
        );

        dssUpdateCaseSubmissionNotification.sendToTribunal(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo(TRIBUNAL_EMAIL_VALUE);
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(UPDATE_RECEIVED_CIC);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }
}
