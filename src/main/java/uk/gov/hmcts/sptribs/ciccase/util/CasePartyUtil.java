package uk.gov.hmcts.sptribs.ciccase.util;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.model.Party;

public final class CasePartyUtil {

    private CasePartyUtil() {
    }

    public static Party determineParty(CaseData caseData, String userEmail) {
        if (caseData == null || caseData.getCicCase() == null || userEmail == null) {
            return null;
        }

        CicCase cicCase = caseData.getCicCase();

        if (userEmail.equalsIgnoreCase(cicCase.getEmail())) {
            return Party.SUBJECT;
        } else if (userEmail.equalsIgnoreCase(cicCase.getApplicantEmailAddress())) {
            return Party.APPLICANT;
        } else if (userEmail.equalsIgnoreCase(cicCase.getRepresentativeEmailAddress())) {
            return Party.REPRESENTATIVE;
        } else if (userEmail.equalsIgnoreCase(cicCase.getRespondentEmail())
            || userEmail.equalsIgnoreCase(cicCase.getAlternativeRespondentEmail())) {
            return Party.RESPONDENT;
        }

        return null;
    }
}
