package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import static uk.gov.hmcts.sptribs.caseworker.util.ContactPartiesReviewUtil.caseworkerSelectedParties;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getSelectedCaseIssueDocuments;

public final class IssueCaseReviewUtil {

    private IssueCaseReviewUtil() {
    }

    public static String issueCaseSelectedParties(CicCase cicCase) {
        return caseworkerSelectedParties(cicCase);
    }

    public static boolean setReviewDocuments(CaseData data) {
        return ReviewDocumentsUtil.setReviewDocuments(data.getCaseIssue(), getSelectedCaseIssueDocuments(data));
    }

    public static void clearReviewDocuments(CaseIssue documents) {
        ReviewDocumentsUtil.clearReviewDocuments(documents);
    }
}
