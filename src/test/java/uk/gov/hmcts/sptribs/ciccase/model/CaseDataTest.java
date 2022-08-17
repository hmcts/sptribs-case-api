package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;

class CaseDataTest {

    @Test
    void shouldReturnApplicant2EmailIfApplicant2EmailIsSet() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsNullAndApplicant2InviteEmailAddressIsSet() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email("")
                .build())
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsBlankAndApplicant2InviteEmailAddressIsSet() {

        final CaseData caseData = CaseData.builder()
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {

        final CaseData caseData = CaseData.builder()
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isNull();
    }

    @Test
    void shouldReturnTrueIfCaseDataIsDivorce() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();

        assertThat(caseData.isDivorce()).isTrue();
    }

    @Test
    void shouldReturnFalseIfCaseDataIsDissolution() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();

        assertThat(caseData.isDivorce()).isFalse();
    }
}
