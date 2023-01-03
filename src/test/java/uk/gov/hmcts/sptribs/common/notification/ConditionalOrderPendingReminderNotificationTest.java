package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.ConditionalOrder;
import uk.gov.hmcts.sptribs.ciccase.model.Solicitor;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getConditionalOrderQuestions;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderPendingReminderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Test
    void shouldSendNotificationToApplicant1WhenSubmittedDateIsNotSet() {
        //Given
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                1234567890123456L,
                caseData.getApplicant1(),
                caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant1(caseData, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendNotificationToApplicant1InWelshWhenSubmittedDateIsNotSetAndApp1LangPrefIsWelsh() {
        //Given
        final var caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                1234567890123456L,
                caseData.getApplicant1(),
                caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant1(caseData, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(WELSH)
        );
    }

    @Test
    void shouldNotSendNotificationToApplicant1WhenSubmittedDateIsSet() {
        //Given
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant1(caseData, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNotificationToApplicant1SolicitorWhenSubmittedDateIsSet() {
        //Given
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YES);
        final var caseData = CaseData.builder().applicant1(applicant).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant1Solicitor(caseData, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationToApplicant2WhenSubmittedDateIsSet() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                1234567890123456L,
                caseData.getApplicant2(),
                caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendNotificationToApplicant2InWelshWhenSubmittedDateIsSetAndApp2LangPrefIsWelsh() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                1234567890123456L,
                caseData.getApplicant2(),
                caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(WELSH)
        );
    }

    @Test
    void shouldNotSendNotificationToApplicant2WhenSubmittedDateIsNotSet() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNotificationToApplicant2WhenApplicant2EmailNotSet() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(null);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant2(caseData, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNotificationToApplicant2SolicitorWhenSubmittedDateIsNotSet() {
        //Given
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YES);
        final var caseData = CaseData.builder().applicant2(applicant).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        //When
        conditionalOrderPendingReminderNotification.sendToApplicant2Solicitor(caseData, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }
}
