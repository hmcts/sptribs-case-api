package uk.gov.hmcts.sptribs.ciccase.util;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.List;

public final class CasePartyUtil {

    public static final List<Party> CITIZEN_CONTACT_PARTIES = List.of(
        Party.APPLICANT,
        Party.REPRESENTATIVE,
        Party.SUBJECT
    );

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
        }

        return null;
    }
}
