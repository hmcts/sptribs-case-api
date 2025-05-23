package uk.gov.hmcts.sptribs.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.document.DocumentClient;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class NotificationServiceCicIT {

    @MockitoBean
    private NotificationClient notificationClient;

    @MockitoBean
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    @MockitoBean
    private DocumentClient caseDocumentClient;

    @Autowired
    private NotificationServiceCIC notificationServiceCIC;

    private static final String NOTIFICATION_RESPONSE_JSON =
        "classpath:responses/notification-response.json";

    @BeforeEach
    void setTestData() {
        Map<String, String> templatesCIC = new HashMap<>();
        templatesCIC.put("APPLICATION_RECEIVED", "5385bfc6-eb33-41f6-ad2b-590a4f427606");
        templatesCIC.put("CASE_ISSUED_CITIZEN_POST", "eedc916f-088f-4653-99ed-b954e1dbd58d");

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templatesCIC);
    }

    @Test
    void shouldSuccessfullySendEmail() throws Exception {
        final Map<String, Object> templateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name"
        );

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("test@email.com")
            .template(APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(false)
            .build();

        final String sendEmailResponseJsonString = expectedResponse(NOTIFICATION_RESPONSE_JSON);
        final SendEmailResponse sendEmailResponse = new SendEmailResponse(sendEmailResponseJsonString);

        when(notificationClient.sendEmail(
            eq("5385bfc6-eb33-41f6-ad2b-590a4f427606"),
            eq("test@email.com"),
            eq(templateVars),
            anyString()
        )).thenReturn(sendEmailResponse);

        NotificationResponse notificationResponse = notificationServiceCIC.sendEmail(request);

        assertThat(notificationResponse.getClientReference()).isEqualTo("ST_CIC email reference");
        assertThat(notificationResponse.getStatus()).isEqualTo("Received");
    }

    @Test
    void shouldThrowNotificationExceptionIfSendEmailFails() throws Exception {
        final Map<String, Object> templateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name"
        );

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("test@email.com")
            .template(APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(false)
            .build();

        doThrow(NotificationClientException.class)
            .when(notificationClient).sendEmail(
                eq("5385bfc6-eb33-41f6-ad2b-590a4f427606"),
                eq("test@email.com"),
                eq(templateVars),
                anyString()
            );

        assertThrows(NotificationException.class, () -> notificationServiceCIC.sendEmail(request));
    }

    @Test
    void shouldSuccessfullySendLetter() throws Exception {
        final Map<String, Object> templateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW"
            );

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("test@email.com")
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(templateVars)
            .build();

        final String sendLetterResponseJsonString = expectedResponse(NOTIFICATION_RESPONSE_JSON);
        final SendLetterResponse sendLetterResponse = new SendLetterResponse(sendLetterResponseJsonString);

        when(notificationClient.sendLetter(
            eq("eedc916f-088f-4653-99ed-b954e1dbd58d"),
            eq(templateVars),
            anyString()
        )).thenReturn(sendLetterResponse);

        NotificationResponse notificationResponse = notificationServiceCIC.sendLetter(request);

        assertThat(notificationResponse.getClientReference()).isEqualTo("ST_CIC email reference");
        assertThat(notificationResponse.getStatus()).isEqualTo("Received");
    }

    @Test
    void shouldThrowNotificationExceptionIfSendLetterFails() throws Exception {
        final Map<String, Object> templateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW"
            );

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("test@email.com")
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(templateVars)
            .build();

        doThrow(NotificationClientException.class)
            .when(notificationClient).sendLetter(
                eq("eedc916f-088f-4653-99ed-b954e1dbd58d"),
                eq(templateVars),
                anyString()
            );

        assertThrows(NotificationException.class, () -> notificationServiceCIC.sendLetter(request));
    }
}
