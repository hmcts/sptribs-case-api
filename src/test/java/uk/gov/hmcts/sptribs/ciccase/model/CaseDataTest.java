package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;

class CaseDataTest {

    @Test
    void shouldReturnApplicant2EmailIfApplicant2EmailIsSet() {
        //When
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        //Then
        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsNullAndApplicant2InviteEmailAddressIsSet() {
        //When
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email("")
                .build())
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        //Then
        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsBlankAndApplicant2InviteEmailAddressIsSet() {
        //When
        final CaseData caseData = CaseData.builder()
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();
        //Then
        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {
        //When
        final CaseData caseData = CaseData.builder()
            .build();
        //Then
        assertThat(caseData.getApplicant2EmailAddress()).isNull();
    }

    @Test
    void shouldReturnTrueIfCaseDataIsDivorce() {

        //When
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();
        //Then
        assertThat(caseData.isDivorce()).isTrue();
    }

    @Test
    void shouldReturnFalseIfCaseDataIsDissolution() {
        //When
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();
        //Then
        assertThat(caseData.isDivorce()).isFalse();
    }
}
