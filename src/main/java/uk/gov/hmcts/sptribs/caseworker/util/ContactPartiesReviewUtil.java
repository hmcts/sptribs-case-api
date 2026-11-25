package uk.gov.hmcts.sptribs.caseworker.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getSelectedContactPartiesDocuments;

public final class ContactPartiesReviewUtil {

    private ContactPartiesReviewUtil() {
    }

    public static String caseworkerSelectedParties(CicCase cicCase) {
        List<String> parties = new ArrayList<>();
        addParty(parties, isNotEmpty(cicCase.getNotifyPartySubject()), "Subject", cicCase.getFullName());
        addParty(parties, isNotEmpty(cicCase.getNotifyPartyApplicant()), "Applicant", cicCase.getApplicantFullName());
        addParty(parties, isNotEmpty(cicCase.getNotifyPartyRepresentative()),
            "Representative", cicCase.getRepresentativeFullName());
        addParty(parties, isNotEmpty(cicCase.getNotifyPartyRespondent()), "Respondent", cicCase.getRespondentName());
        return String.join("\n", parties);
    }

    public static String respondentSelectedParties(CicCase cicCase, ContactParties contactParties) {
        List<String> parties = new ArrayList<>();
        addParty(parties, isNotEmpty(contactParties.getSubjectContactParties()), "Subject", cicCase.getFullName());
        addParty(parties, isNotEmpty(contactParties.getApplicantContactParties()),
            "Applicant", cicCase.getApplicantFullName());
        addParty(parties, isNotEmpty(contactParties.getRepresentativeContactParties()),
            "Representative", cicCase.getRepresentativeFullName());
        addParty(parties, isNotEmpty(contactParties.getTribunal()), "Tribunal", null);
        return String.join("\n", parties);
    }

    public static boolean setReviewDocuments(CaseData data) {
        return ReviewDocumentsUtil.setReviewDocuments(data.getContactPartiesDocuments(), getSelectedContactPartiesDocuments(data));
    }

    public static void clearReviewDocuments(ContactPartiesDocuments documents) {
        ReviewDocumentsUtil.clearReviewDocuments(documents);
    }

    private static void addParty(List<String> parties, boolean isSelected, String role, String name) {
        if (isSelected) {
            parties.add(StringUtils.isBlank(name) ? role : role + ": " + name);
        }
    }
}
