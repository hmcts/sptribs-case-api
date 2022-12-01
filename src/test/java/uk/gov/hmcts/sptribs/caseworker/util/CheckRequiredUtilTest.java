package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CheckRequiredUtilTest {

    @Test
    void shouldSuccessfullyCheckNullSubjectRepresentativeRespondent() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldSuccessfullyCheckNullSubjectRepresentativeRespondentWithRepresentatvie() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckNullSubjectRepresentativeRespondentWithSubject() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckNullSubjectRepresentativeRespondentWithRespondent() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT));
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckNullSubjectRepresentativeApplicant() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicant() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckNullFlagSubjectRepresentativeApplicant() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullFlagSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldSuccessfullyCheckNullFlagSubjectRepresentativeApplicantWithRepresentative() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setFlagPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullFlagSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckNullFlagSubjectRepresentativeApplicantWithSubject() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setFlagPartySubject(Set.of(SubjectCIC.SUBJECT));
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullFlagSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckNullFlagSubjectRepresentativeApplicantWithApplicant() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = new CicCase();
        cicCase.setFlagPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC));
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkNullFlagSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicantWithApplicant() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = CicCase.builder()
            .applicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC))
            .flagPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicantWithRepresentative() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = CicCase.builder()
            .flagPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicantWithRepresentativeSubject() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = CicCase.builder()
            .flagPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .flagPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicantWithRepresentativeApplicantSubject() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = CicCase.builder()
            .flagPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .flagPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .flagPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicantWithRepresentativeApplicant() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = CicCase.builder()
            .flagPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .flagPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isTrue();
    }


    @Test
    void shouldSuccessfullyCheckMultiSubjectRepresentativeApplicantWithSubject() {
        //Given
        final CaseData caseData = new CaseData();
        final CicCase cicCase = CicCase.builder()
            .flagPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        caseData.setCicCase(cicCase);

        //When
        boolean result = CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant(caseData);

        //Then
        assertThat(result).isFalse();
    }
}
