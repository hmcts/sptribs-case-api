package uk.gov.hmcts.sptribs.ciccase.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.notification.model.Party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CasePartyUtilTest {

    @Test
    public void shouldReturnNullWhenCaseDataOrCicCaseOrEmailIsNull() {
        assertNull(CasePartyUtil.determineParty(null, "email@test.com"));
        assertNull(CasePartyUtil.determineParty(CriminalInjuriesCompensationData.builder().build(), "email@test.com"));
        assertNull(
            CasePartyUtil.determineParty(CriminalInjuriesCompensationData.builder().cicCase(CicCase.builder().build()).build(), null)
        );
    }

    @Test
    public void shouldReturnSubjectWhenEmailMatchesSubject() {
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(CicCase.builder()
                .email("subject@test.com")
                .build())
            .build();

        assertEquals(Party.SUBJECT, CasePartyUtil.determineParty(caseData, "subject@test.com"));
        assertEquals(Party.SUBJECT, CasePartyUtil.determineParty(caseData, "SUBJECT@TEST.COM"));
    }

    @Test
    public void shouldReturnApplicantWhenEmailMatchesApplicant() {
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(CicCase.builder()
                .applicantEmailAddress("applicant@test.com")
                .build())
            .build();

        assertEquals(Party.APPLICANT, CasePartyUtil.determineParty(caseData, "applicant@test.com"));
    }

    @Test
    public void shouldReturnRepresentativeWhenEmailMatchesRepresentative() {
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(CicCase.builder()
                .representativeEmailAddress("representative@test.com")
                .build())
            .build();

        assertEquals(Party.REPRESENTATIVE, CasePartyUtil.determineParty(caseData, "representative@test.com"));
    }


    @Test
    public void shouldReturnNullWhenEmailDoesNotMatchAnyParty() {
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(CicCase.builder()
                .email("subject@test.com")
                .build())
            .build();

        assertNull(CasePartyUtil.determineParty(caseData, "other@test.com"));
    }
}
