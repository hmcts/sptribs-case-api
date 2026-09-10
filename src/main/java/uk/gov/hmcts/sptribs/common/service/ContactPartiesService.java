package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.document.service.CorrespondenceDocumentService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.dispatcher.NewOrderIssuedNotification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactPartiesService {

    @FunctionalInterface
    public interface NotificationSender {
        String send();
    }

    private static final int DOCUMENT_ATTACHMENT_LIMIT = 10;

    private final DocumentsService documentsService;
    private final CorrespondenceDocumentService correspondenceDocumentService;
    private final NotificationHelper notificationHelper;

    public void sendOrderNotification(String caseNumber, CaseData caseData,
                                      NewOrderIssuedNotification newOrderIssuedNotification) {
        Map<String, String> uploadedDocuments = notificationHelper.buildDocumentList(
            caseData.getContactPartiesDocuments().getDocumentList(),
            DOCUMENT_ATTACHMENT_LIMIT
        );
        Map<NotificationParties, NotificationSender> sendersByParty = new LinkedHashMap<>();
        sendersByParty.put(
            NotificationParties.SUBJECT,
            () -> newOrderIssuedNotification.sendToSubject(caseData, caseNumber, uploadedDocuments)
        );
        sendersByParty.put(
            NotificationParties.REPRESENTATIVE,
            () -> newOrderIssuedNotification.sendToRepresentative(caseData, caseNumber, uploadedDocuments)
        );
        sendersByParty.put(
            NotificationParties.RESPONDENT,
            () -> newOrderIssuedNotification.sendToRespondent(caseData, caseNumber, uploadedDocuments)
        );
        sendersByParty.put(
            NotificationParties.APPLICANT,
            () -> newOrderIssuedNotification.sendToApplicant(caseData, caseNumber, uploadedDocuments)
        );
        List<String> correspondenceIds = sendNotificationsToSelectedParties(caseData, sendersByParty);

        if (!correspondenceIds.isEmpty()) {
            linkCorrespondenceIdsToDocuments(caseData, uploadedDocuments, correspondenceIds);
        }
    }

    public List<String> sendNotificationsToSelectedParties(CaseData caseData,
                                                            Map<NotificationParties, NotificationSender> sendersByParty) {
        List<String> correspondenceIds = new ArrayList<>();

        sendersByParty.forEach((party, sender) -> {
            if (isPartySelected(caseData, party)) {
                addCorrespondenceId(correspondenceIds, sender.send());
            }
        });

        return correspondenceIds;
    }

    public void linkCorrespondenceIdsToDocuments(CaseData caseData, Map<String, String> uploadedDocuments,
                                                 List<String> correspondenceIds) {
        try {
            List<Long> documentIds =
                documentsService.getDocumentsViaSentByContactParties(caseData, uploadedDocuments);

            for (String correspondenceId : correspondenceIds) {
                try {
                    correspondenceDocumentService.saveCorrespondenceDocumentLink(correspondenceId, documentIds);
                } catch (CorrespondenceDocumentSaveException e) {
                    log.error(
                        "Unable to link documents for correspondenceId {}. Continuing with remaining correspondences.",
                        correspondenceId,
                        e
                    );
                }
            }
        } catch (DocumentLookupException e) {
            log.error(
                "Notifications were sent successfully, but document IDs could not be retrieved. "
                    + "Skipping correspondence document linking.",
                e
            );
        }
    }

    private void addCorrespondenceId(List<String> correspondenceIds, String correspondenceId) {
        if (correspondenceId != null) {
            correspondenceIds.add(correspondenceId);
        }
    }

    private boolean isPartySelected(CaseData caseData, NotificationParties party) {
        return switch (party) {
            case SUBJECT -> !CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject());
            case REPRESENTATIVE -> !CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative());
            case RESPONDENT -> !CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent());
            case APPLICANT -> !CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyApplicant());
        };
    }
}
