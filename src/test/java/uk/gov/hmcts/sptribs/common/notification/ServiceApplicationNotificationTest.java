package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationTemplateException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.sptribs.notification.CommonContent.NO;
import static uk.gov.hmcts.sptribs.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.YES;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.sptribs.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ServiceApplicationNotification serviceApplicationNotification;

    private static final YesOrNo NOT_GRANTED = YesOrNo.NO;
    private static final YesOrNo GRANTED = YesOrNo.YES;

    private static final Long ID = 1234567890123456L;

    @Test
    void shouldSendDeemedAsServedServiceApplicationRejectedEmailToSoleApplicantWithDivorceContent() {
        //When
        sendNotification(DEEMED, DIVORCE, NOT_GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendDeemedAsServedServiceApplicationRejectedEmailToSoleApplicantWithDissolutionContent() {
        //When
        sendNotification(DEEMED, DISSOLUTION, NOT_GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendDispensedServiceApplicationRejectedEmailToSoleApplicantWithDivorceContent() {
        //When
        sendNotification(DISPENSED, DIVORCE, NOT_GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendDispensedServiceApplicationRejectedEmailInWelsh() {
        //Given
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getAlternativeService().setAlternativeServiceType(DISPENSED);
        data.getAlternativeService().setServiceApplicationGranted(NOT_GRANTED);

        final Map<String, String> templateVars = getMainTemplateVars();

        when(commonContent.mainTemplateVars(data, ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        //When
        serviceApplicationNotification.sendToApplicant1(data, ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(WELSH)
        );
    }

    @Test
    void shoulThrowTemplaceException() {
        //Given
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getAlternativeService().setAlternativeServiceType(DISPENSED);

        //When&Then
        assertThatThrownBy(() -> serviceApplicationNotification.sendToApplicant1(
                data, ID
            )
        )
            .isInstanceOf(NotificationTemplateException.class)
            .hasMessageContaining("MISSING_FIELD_MESSAGE");

    }

    @Test
    void shouldSendDispensedServiceApplicationRejectedEmailToSoleApplicantWithDissolutionContent() {
        //When
        sendNotification(DISPENSED, DISSOLUTION, NOT_GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationRejectedEmailToSoleApplicantWithDivorceContent() {
        //When
        sendNotification(BAILIFF, DIVORCE, NOT_GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationRejectedEmailToSoleApplicantWithDissolutionContent() {
        //When
        sendNotification(BAILIFF, DISSOLUTION, NOT_GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationSuccessfulEmailToSoleApplicant() {
        //When
        sendNotification(BAILIFF, DIVORCE, GRANTED);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_GRANTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationSuccessfulEmailToSoleApplicantWhenLanguagePrefIsWelsh() {
        //Given
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getAlternativeService().setAlternativeServiceType(BAILIFF);
        data.getAlternativeService().setServiceApplicationGranted(GRANTED);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(data, ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        //When
        serviceApplicationNotification.sendToApplicant1(data, ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_GRANTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH)
        );
    }

    @Disabled
    @Test
    void getEmailTemplateShouldThrowErrorIfServiceApplicationGrantedIsNull() {
        assertThatExceptionOfType(NotificationTemplateException.class)
            .isThrownBy(() -> sendNotification(BAILIFF, DIVORCE, null))
            .withMessage("Notification failed with missing field 'serviceApplicationGranted' for Case Id: 1234567890123456");
    }

    private void sendNotification(AlternativeServiceType alternativeServiceType, DivorceOrDissolution divorceOrDissolution,
                                  YesOrNo applicationGranted) {

        CaseData data = validApplicant1CaseData();
        data.getAlternativeService().setAlternativeServiceType(alternativeServiceType);
        data.getAlternativeService().setServiceApplicationGranted(applicationGranted);

        final Map<String, String> templateVars = getMainTemplateVars();

        if (DISSOLUTION.equals(divorceOrDissolution)) {
            templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        }

        if (applicationGranted != null) {
            when(commonContent.mainTemplateVars(data, ID, data.getApplicant1(), data.getApplicant2()))
                .thenReturn(templateVars);
        }

        serviceApplicationNotification.sendToApplicant1(data, ID);
    }
}
