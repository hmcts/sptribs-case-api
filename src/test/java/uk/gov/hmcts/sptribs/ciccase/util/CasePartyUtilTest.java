package uk.gov.hmcts.sptribs.ciccase.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.model.Party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CasePartyUtilTest {

    @Test
    public void shouldReturnNullWhenCaseDataOrCicCaseOrEmailIsNull() {
        assertNull(CasePartyUtil.determineParty(null, "email@test.com"));
        assertNull(CasePartyUtil.determineParty(CaseData.builder().build(), "email@test.com"));
        assertNull(CasePartyUtil.determineParty(CaseData.builder().cicCase(CicCase.builder().build()).build(), null));
    }

    @Test
    public void shouldReturnSubjectWhenEmailMatchesSubject() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .email("subject@test.com")
                .build())
            .build();

        assertEquals(Party.SUBJECT, CasePartyUtil.determineParty(caseData, "subject@test.com"));
        assertEquals(Party.SUBJECT, CasePartyUtil.determineParty(caseData, "SUBJECT@TEST.COM"));
    }

    @Test
    public void shouldReturnApplicantWhenEmailMatchesApplicant() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantEmailAddress("applicant@test.com")
                .build())
            .build();

        assertEquals(Party.APPLICANT, CasePartyUtil.determineParty(caseData, "applicant@test.com"));
    }

    @Test
    public void shouldReturnRepresentativeWhenEmailMatchesRepresentative() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeEmailAddress("representative@test.com")
                .build())
            .build();

        assertEquals(Party.REPRESENTATIVE, CasePartyUtil.determineParty(caseData, "representative@test.com"));
    }


    @Test
    public void shouldReturnNullWhenEmailDoesNotMatchAnyParty() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .email("subject@test.com")
                .build())
            .build();

        assertNull(CasePartyUtil.determineParty(caseData, "other@test.com"));
    }
}
