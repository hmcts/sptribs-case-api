package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.organisationPolicy;

class AcknowledgementOfServiceTest {

    @Test
    void shouldSetNoticeOfProceedingsToSolicitorIfApplicantIsRepresented() {
        //Given
        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build();
        final Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        //When
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();
        acknowledgementOfService.setNoticeOfProceedings(applicant);

        //Then
        assert (!acknowledgementOfService.isDisputed());
        assert (!acknowledgementOfService.hasApplicantBeenNotifiedDisputeFormOverdue());
        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isEqualTo(TEST_ORG_NAME);
    }

    @Test
    void shouldSetNoticeOfProceedingsToSolicitorWithNoOrganisationPolicy() {
        //Given
        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .firmName(TEST_ORG_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build();
        final Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        //When
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(applicant);

        //Then
        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isEqualTo(TEST_ORG_NAME);
    }

    @Test
    void shouldSetNoticeOfProceedingsEmailToApplicantIfApplicantNotIsRepresented() {
        //Given
        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build();
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .email(TEST_USER_EMAIL)
            .build();

        //When
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(applicant);

        //Then
        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_USER_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isNullOrEmpty();
    }
}
