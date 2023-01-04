package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicationType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.NO;
import static uk.gov.hmcts.sptribs.notification.CommonContent.YES;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CITIZEN_APPLICATION_WITHDRAWN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
public class ApplicationWithdrawnNotificationTest {
    private static final String IS_RESPONDENT = "isRespondent";
    private static final String RESPONDENT_PARTNER = "respondentPartner";

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationWithdrawnNotification applicationWithdrawnNotification;

    @Test
    void shouldSendEmailToSoleApplicant1WithDivorceContent() {
        //Given
        CaseData data = validCaseDataForIssueApplication();

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        //When
        applicationWithdrawnNotification.sendToApplicant1(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleApplicant1WithDissolutionContent() {
        //Given
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(dissolutionTemplateVars);

        //When
        applicationWithdrawnNotification.sendToApplicant1(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithDivorceContent() {
        //Given
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);
        when(commonContent.getPartner(data, data.getApplicant2())).thenReturn("husband");

        //When
        applicationWithdrawnNotification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, YES),
                hasEntry(RESPONDENT_PARTNER, "husband")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
        verify(commonContent).getPartner(data, data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithDissolutionContent() {
        //Given
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setDivorceOrDissolution(DISSOLUTION);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);
        when(commonContent.getPartner(data, data.getApplicant2())).thenReturn("husband");

        //When
        applicationWithdrawnNotification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_RESPONDENT, YES),
                hasEntry(RESPONDENT_PARTNER, "husband")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
        verify(commonContent).getPartner(data, data.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithDivorceContent() {
        //Given
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        //When
        applicationWithdrawnNotification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant2WithDissolutionContent() {
        //Given
        CaseData data = validCaseDataForIssueApplication();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.setDivorceOrDissolution(DISSOLUTION);

        Map<String, String> dissolutionTemplateVars = new HashMap<>(getMainTemplateVars());
        dissolutionTemplateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(dissolutionTemplateVars);

        //When
        applicationWithdrawnNotification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLICATION_WITHDRAWN),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_RESPONDENT, NO),
                hasEntry(RESPONDENT_PARTNER, "")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendEmailApplicant2IfEmailIsNull() {
        //Given
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail(null);

        //When
        applicationWithdrawnNotification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailApplicant2IfEmailIsEmptyString() {
        //Given
        CaseData data = validApplicant1CaseData();
        data.getApplicant2().setEmail("");

        //When
        applicationWithdrawnNotification.sendToApplicant2(data, 1234567890123456L);

        //Then
        verifyNoInteractions(notificationService);
    }
}
