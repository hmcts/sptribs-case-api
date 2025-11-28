package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class DssUpdateCaseSubmissionNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private DssUpdateCaseSubmissionNotification dssUpdateCaseSubmissionNotification;

    @Test
    void shouldSendEmailToApplicant() {
        final CicCase cicCase = CicCase.builder().email(TEST_APPLICANT_EMAIL).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final String caseReference = String.valueOf(TEST_CASE_ID);
        final Map<String, Object> templateVars = new HashMap<>();
        final NotificationRequest notificationRequest = NotificationRequest.builder()
            .destinationAddress(TEST_APPLICANT_EMAIL)
            .template(UPDATE_RECEIVED)
            .templateVars(templateVars)
            .build();

        when(notificationHelper.getSubjectCommonVars(caseReference, caseData))
            .thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(
            TEST_APPLICANT_EMAIL,
            templateVars,
                UPDATE_RECEIVED
        )).thenReturn(notificationRequest);

        dssUpdateCaseSubmissionNotification.sendToApplicant(caseData, caseReference);

        verify(notificationService).sendEmail(notificationRequest, caseReference);
    }

    @Test
    void shouldSendEmailToTribunal() {
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder().cicCase(cicCase).build();
        final String caseReference = String.valueOf(TEST_CASE_ID);
        final Map<String, Object> templateVars = new HashMap<>();
        final NotificationRequest notificationRequest = NotificationRequest.builder()
            .destinationAddress(TRIBUNAL_EMAIL_VALUE)
            .template(UPDATE_RECEIVED_CIC)
            .templateVars(templateVars)
            .build();

        when(notificationHelper.getTribunalCommonVars(caseReference, caseData))
            .thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(
            TRIBUNAL_EMAIL_VALUE,
            templateVars,
                UPDATE_RECEIVED_CIC
        )).thenReturn(notificationRequest);

        dssUpdateCaseSubmissionNotification.sendToTribunal(caseData, String.valueOf(TEST_CASE_ID));

        verify(notificationService).sendEmail(notificationRequest, String.valueOf(TEST_CASE_ID));
    }
}
