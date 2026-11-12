package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ReviewDocumentSlots;
import uk.gov.hmcts.sptribs.caseworker.model.Slot;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ReviewDocumentsUtil {

    private ReviewDocumentsUtil() {
    }

    public static boolean setReviewDocuments(ReviewDocumentSlots review, Optional<List<ListValue<CaseworkerCICDocument>>> selection) {
        if (review == null || selection.isEmpty()) {
            clearReviewDocuments(review);
            return false;
        }

        List<ListValue<CaseworkerCICDocument>> documents = selection.orElseThrow();
        Set<Slot> populatedSlots = EnumSet.noneOf(Slot.class);
        Slot[] slots = Slot.values();
        populatedSlots.addAll(Arrays.asList(slots).subList(0, Math.min(documents.size(), slots.length)));
        review.setActiveDocumentSlots(populatedSlots);
        review.setD01(documentAt(documents, 0));
        review.setD02(documentAt(documents, 1));
        review.setD03(documentAt(documents, 2));
        review.setD04(documentAt(documents, 3));
        review.setD05(documentAt(documents, 4));
        review.setD06(documentAt(documents, 5));
        review.setD07(documentAt(documents, 6));
        review.setD08(documentAt(documents, 7));
        review.setD09(documentAt(documents, 8));
        review.setD10(documentAt(documents, 9));
        return true;
    }

    public static void clearReviewDocuments(ReviewDocumentSlots review) {
        if (review == null) {
            return;
        }
        review.setActiveDocumentSlots(null);
        review.setD01(null);
        review.setD02(null);
        review.setD03(null);
        review.setD04(null);
        review.setD05(null);
        review.setD06(null);
        review.setD07(null);
        review.setD08(null);
        review.setD09(null);
        review.setD10(null);
    }

    private static CaseworkerCICDocument documentAt(List<ListValue<CaseworkerCICDocument>> documents, int index) {
        return documents.size() <= index ? null : documents.get(index).getValue();
    }
}
