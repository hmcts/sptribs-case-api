package uk.gov.hmcts.sptribs.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OrderNotificationUtil {

    private OrderNotificationUtil() {

    }

    public static void sendOrderNotification(
        String caseNumber,
        CaseData caseData,
        int docAttachLimit,
        NotificationHelper notificationHelper,
        ContactPartiesNotification contactPartiesNotification,
        ContactPartiesService contactPartiesService
    ) {
        if (caseData == null
            || caseData.getCicCase() == null
            || caseData.getContactPartiesDocuments() == null
            || caseData.getContactPartiesDocuments().getDocumentList() == null) {
            return;
        }

        final Map<String, String> uploadedDocuments = notificationHelper
            .buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), docAttachLimit);

        List<String> correspondenceIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            correspondenceIds.add(contactPartiesNotification.sendToSubject(caseData, caseNumber, uploadedDocuments));
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            correspondenceIds.add(contactPartiesNotification.sendToRepresentative(caseData, caseNumber, uploadedDocuments));
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            correspondenceIds.add(contactPartiesNotification.sendToRespondent(caseData, caseNumber, uploadedDocuments));
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyApplicant())) {
            correspondenceIds.add(contactPartiesNotification.sendToApplicant(caseData, caseNumber, uploadedDocuments));
        }

        if (!correspondenceIds.isEmpty()) {
            contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, uploadedDocuments, correspondenceIds);
        }
    }
}
