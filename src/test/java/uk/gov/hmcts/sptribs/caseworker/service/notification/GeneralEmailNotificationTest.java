package uk.gov.hmcts.sptribs.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.GeneralEmail;
import uk.gov.hmcts.sptribs.ciccase.model.MarriageDetails;
import uk.gov.hmcts.sptribs.ciccase.model.Solicitor;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.notification.TemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.sptribs.notification.TemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.sptribs.notification.TemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.sptribs.notification.TemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
public class GeneralEmailNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GeneralEmailNotification generalEmailNotification;

    @Test
    public void shouldSendEmailNotificationToApplicantWhenGeneralEmailPartyIsPetitionerAndIsNotSolicitorRepresented() {
        //Given
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(APPLICANT)
            .build()
        );

        //When
        generalEmailNotification.send(caseData, TEST_CASE_ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_PETITIONER),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentWhenGeneralEmailPartyIsRespondentAndIsNotSolicitorRepresented() {
        //Given
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(RESPONDENT)
            .build()
        );

        //When
        generalEmailNotification.send(caseData, TEST_CASE_ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_RESPONDENT),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldNotSendEmailNotificationWhenGeneralEmailPartyIsRespondentAndIsNotSolicitorRepresentedAndRespondentEmailIsNotPresent() {
        //Given
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setEmail(null);
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(RESPONDENT)
            .build()
        );

        //When
        generalEmailNotification.send(caseData, TEST_CASE_ID);

        //Then
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationToApplicantSolicitorWhenGeneralEmailPartyIsPetitionerAndIsSolicitorRepresented() {
        //Given
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant1.setSolicitorRepresented(YES);
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(APPLICANT)
            .build()
        );

        //When
        generalEmailNotification.send(caseData, TEST_CASE_ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(GENERAL_EMAIL_PETITIONER_SOLICITOR),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentSolicitorWhenGeneralEmailPartyIsRespondentAndIsSolicitorRepresented() {
        //Given
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant2.setSolicitorRepresented(YES);
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(RESPONDENT)
            .build()
        );

        //When
        generalEmailNotification.send(caseData, TEST_CASE_ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(GENERAL_EMAIL_RESPONDENT_SOLICITOR),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailNotificationToOtherPartyWhenGeneralEmailPartyIsOther() throws Exception {
        //Given
        final var caseData = caseData();

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailOtherRecipientEmail(TEST_USER_EMAIL)
            .generalEmailOtherRecipientName("otherparty")
            .generalEmailParties(OTHER)
            .build()
        );

        //When
        generalEmailNotification.send(caseData, TEST_CASE_ID);

        //Then
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_OTHER_PARTY),
            anyMap(),
            eq(ENGLISH)
        );
    }
}
