package uk.gov.hmcts.sptribs.notification;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;

import java.util.Map;

public interface PartiesNotification {
    default void sendToSubject(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToSubject(
        final CaseData caseData,
        final String caseNumber,
        final Map<String, String> uploadedDocuments
    ) {
        sendToSubject(caseData, caseNumber);
    }

    default void sendToSubject(final DssCaseData dssCaseData, final String caseNumber) {
        //No operation
    }

    default void sendToApplicant(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToApplicant(
        final CaseData caseData,
        final String caseNumber,
        final Map<String, String> uploadedDocuments
    ) {
        sendToApplicant(caseData, caseNumber);
    }


    default void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToRepresentative(
        final CaseData caseData,
        final String caseNumber,
        final Map<String, String> uploadedDocuments
    ) {
        sendToRepresentative(caseData, caseNumber);
    }

    default void sendToRepresentative(final DssCaseData dssCaseData, final String caseNumber) {
        //No operation
    }

    default void sendToRespondent(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToRespondent(
        final CaseData caseData,
        final String caseNumber,
        final Map<String, String> uploadedDocuments
    ) {
        sendToRespondent(caseData, caseNumber);
    }

    default void sendToTribunal(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    default void sendToTribunal(
        final CaseData caseData,
        final String caseNumber,
        final Map<String, String> uploadedDocuments
    ) {
        sendToTribunal(caseData, caseNumber);
    }

}
