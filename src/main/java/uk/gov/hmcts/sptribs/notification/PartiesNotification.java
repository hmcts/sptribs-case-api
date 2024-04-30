package uk.gov.hmcts.sptribs.notification;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;

public interface PartiesNotification {
    default void sendToSubject(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToSubject(final DssCaseData dssCaseData, final String caseNumber) {
        //No operation
    }

    default void sendToApplicant(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToRepresentative(final DssCaseData dssCaseData, final String caseNumber) {
        //No operation
    }

    default void sendToRespondent(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToTribunal(final CaseData caseData, final String caseNumber) {
        //No operation
    }

}
