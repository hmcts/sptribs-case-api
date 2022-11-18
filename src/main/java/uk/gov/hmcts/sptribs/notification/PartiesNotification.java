package uk.gov.hmcts.sptribs.notification;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

public interface PartiesNotification {
    default void sendToSubject(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToRepresentative(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToRespondent(final CaseData caseData, final Long caseId) {
        //No operation
    }
}
