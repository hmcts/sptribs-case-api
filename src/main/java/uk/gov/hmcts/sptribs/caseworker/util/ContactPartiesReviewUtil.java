package uk.gov.hmcts.sptribs.caseworker.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.Slot;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
        ContactPartiesDocuments reviewDocuments = data.getContactPartiesDocuments();
        List<ListValue<CaseworkerCICDocument>> documents = getSelectedContactPartiesDocuments(data).orElse(null);
        if (documents == null) {
            clearReviewDocuments(reviewDocuments);
            return false;
        }
        Set<Slot> populatedSlots = EnumSet.noneOf(Slot.class);
        Slot[] slots = Slot.values();
        for (int index = 0; index < Math.min(documents.size(), slots.length); index++) {
            populatedSlots.add(slots[index]);
        }
        reviewDocuments.setActiveDocumentSlots(populatedSlots);
        reviewDocuments.setD01(documentAt(documents, 0));
        reviewDocuments.setD02(documentAt(documents, 1));
        reviewDocuments.setD03(documentAt(documents, 2));
        reviewDocuments.setD04(documentAt(documents, 3));
        reviewDocuments.setD05(documentAt(documents, 4));
        reviewDocuments.setD06(documentAt(documents, 5));
        reviewDocuments.setD07(documentAt(documents, 6));
        reviewDocuments.setD08(documentAt(documents, 7));
        reviewDocuments.setD09(documentAt(documents, 8));
        reviewDocuments.setD10(documentAt(documents, 9));
        return true;
    }

    public static void clearReviewDocuments(ContactPartiesDocuments documents) {
        if (documents == null) {
            return;
        }
        documents.setActiveDocumentSlots(null);
        documents.setD01(null);
        documents.setD02(null);
        documents.setD03(null);
        documents.setD04(null);
        documents.setD05(null);
        documents.setD06(null);
        documents.setD07(null);
        documents.setD08(null);
        documents.setD09(null);
        documents.setD10(null);
    }

    private static CaseworkerCICDocument documentAt(List<ListValue<CaseworkerCICDocument>> documents, int index) {
        return documents == null || documents.size() <= index ? null : documents.get(index).getValue();
    }

    private static void addParty(List<String> parties, boolean isSelected, String role, String name) {
        if (isSelected) {
            parties.add(StringUtils.isBlank(name) ? role : role + ": " + name);
        }
    }
}
