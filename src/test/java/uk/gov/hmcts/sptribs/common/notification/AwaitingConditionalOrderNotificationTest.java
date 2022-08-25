package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.Solicitor;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.SOLICITOR_AWAITING_CONDITIONAL_ORDER;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class AwaitingConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private AwaitingConditionalOrderNotification notification;

    @Test
    void shouldSendEmailToApplicant1() {
        //Given
        final var data = validApplicant1CaseData();
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(new HashMap<>());

        //When
        notification.sendToApplicant1(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1InWelshIfApp1LangPrefIsWelsh() {
        //Given
        final var data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(new HashMap<>());

        //When
        notification.sendToApplicant1(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.NO)
            )),
            eq(WELSH)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1Solicitor() {
        //Given
        final var applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YES);
        final var data = CaseData.builder().applicant1(applicant).build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(1234567890123456L)).thenReturn(SIGN_IN_PROFESSIONAL_USERS_URL);
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        //When
        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(UNION_TYPE, DIVORCE),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_REFERENCE, "not provided"),
                hasEntry(SIGN_IN_URL, SIGN_IN_PROFESSIONAL_USERS_URL)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithReference() {
        //Given
        final var applicant = getApplicant();
        applicant.setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .name(TEST_SOLICITOR_NAME)
                .reference("ref")
                .build()
        );
        applicant.setSolicitorRepresented(YES);
        final var data = CaseData.builder().applicant1(applicant).build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        //When
        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_REFERENCE, "ref")
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2IfJointApplication() {
        //Given
        final var data = validApplicant2CaseData();
        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(new HashMap<>());

        //When
        notification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2IfJointApplicationInWelshIfApp2LangPrefIsWelsh() {
        //Given
        final var data = validApplicant2CaseData();
        data.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(new HashMap<>());

        //When
        notification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.NO)
            )),
            eq(WELSH)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendEmailToApplicant2IfSoleApplication() {
        //Given
        final var data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);

        //When
        notification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService, commonContent);
    }

    @Test
    void shouldNotSendEmailToApplicant2IfEmailNotSet() {
        //Given
        final var data = validApplicant2CaseData();
        data.getApplicant2().setEmail(null);

        //When
        notification.sendToApplicant2(data, 1234567890123456L);

        //When
        verifyNoInteractions(notificationService, commonContent);
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWhenJointApplication() {
        //Given
        final Applicant applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YES);
        final var data = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant2(applicant)
            .build();

        when(commonContent.basicTemplateVars(data, 1234567890123456L)).thenReturn(getBasicTemplateVars());

        //When
        notification.sendToApplicant2Solicitor(data, 1234567890123456L);

        //When
        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_AWAITING_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456")
            )),
            eq(ENGLISH)
        );
    }
}
