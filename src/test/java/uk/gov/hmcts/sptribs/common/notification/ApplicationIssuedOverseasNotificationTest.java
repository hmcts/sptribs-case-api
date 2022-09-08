package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseInvite;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.sptribs.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.sptribs.notification.CommonContent.NO;
import static uk.gov.hmcts.sptribs.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.YES;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class ApplicationIssuedOverseasNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationIssuedOverseasNotification applicationIssuedOverseasNotification;

    @Test
    void shouldNotifyApplicantOfServiceToOverseasRespondentWithEmail() {
        //Given
        final CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        //When
        applicationIssuedOverseasNotification.sendToApplicant1(data, 1234567890123456L);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(REVIEW_DEADLINE_DATE, LocalDate.now().plusDays(28).format(DATE_TIME_FORMATTER)),
                hasEntry(COURT_EMAIL, "courtEmail")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotifyApplicantInWelshOfServiceToOverseasRespondentWithEmail() {
        final CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        applicationIssuedOverseasNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED),
            anyMap(),
            eq(WELSH)
        );

        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotifyApplicantOfServiceToOverseasRespondentWithoutEmail() {
        final CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());
        data.setCaseInvite(new CaseInvite(null, null, null));
        data.getApplicant2().setEmail(null);

        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        applicationIssuedOverseasNotification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(FIRST_NAME, TEST_FIRST_NAME),
                hasEntry(LAST_NAME, TEST_LAST_NAME),
                hasEntry(PARTNER, "partner"),
                hasEntry(REVIEW_DEADLINE_DATE, LocalDate.now().plusDays(28).format(DATE_TIME_FORMATTER)),
                hasEntry(COURT_EMAIL, "courtEmail")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }
}
