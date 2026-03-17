package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentRemoveListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.SendOrderUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.dto.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersListRestoreService {

    private final CaseEventRepository caseEventRepository;

    public void restoreOrdersList(Long reference, CaseData currentData, LocalDate startDate, LocalDate endDate) {

        List<RemoveEventWithPrecedingData> removeEvents = caseEventRepository
            .getRemoveEventsWithPrecedingData(reference, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, startDate, endDate);

        if (removeEvents.isEmpty()) {
            log.info("No remove events found for reference={}, nothing to restore", reference);
            return;
        }

        removeEvents.forEach(event ->
            DocumentRemoveListUtil.setDocumentsListForRemoval(event.getCurrentEventData(), event.getPrecedingEventData()));

        List<ListValue<Order>> currentOrdersList = currentData.getCicCase().getOrderList();

        List<ListValue<Order>> restoredOrdersList = removeEvents.stream()
            .sorted(Comparator.comparing(RemoveEventWithPrecedingData::getCurrentEventDate))
            .filter(event -> hasOrdersList(event.getPrecedingEventData()))
            .flatMap(event -> findRemovedEntries(event).stream())
            .filter(entry -> !currentOrdersList.contains(entry))
            .distinct()
            .toList();

        if (restoredOrdersList.isEmpty()) {
            log.info("No missing entries found for reference={}, nothing to restore", reference);
            return;
        }

        log.info("Restoring {} ordersList entries for reference={}",
            restoredOrdersList.size(), reference);

        restoredOrdersList.stream().map(ListValue::getValue).forEach(order -> SendOrderUtil.updateCicCaseOrderList(currentData, order));
    }

    private List<ListValue<Order>> findRemovedEntries(RemoveEventWithPrecedingData event) {
        List<ListValue<Order>> before = event.getPrecedingEventData().getCicCase().getOrderList();
        List<ListValue<Order>> after = event.getCurrentEventData().getCicCase().getOrderList();

        Set<Document> removedDocuments = event.getCurrentEventData().getCicCase()
            .getRemovedDocumentList()
            .stream()
            .map(ListValue::getValue)
            .map(CaseworkerCICDocument::getDocumentLink)
            .collect(Collectors.toSet());


        return before.stream()
            .filter(entry -> !after.contains(entry))
            .filter(entry -> !orderContainsRemovedDocument(entry.getValue(), removedDocuments))
            .toList();
    }

    private boolean hasOrdersList(CaseData data) {
        return data != null
            && data.getCicCase().getOrderList() != null
            && !data.getCicCase().getOrderList().isEmpty();
    }

    private boolean orderContainsRemovedDocument(Order order, Set<Document> removedDocuments) {
        if (order.getDraftOrder() != null
                && order.getDraftOrder().getTemplateGeneratedDocument() != null) {
            return removedDocuments.contains(order.getDraftOrder().getTemplateGeneratedDocument());
        }

        if (order.getUploadedFile() != null && !order.getUploadedFile().isEmpty()) {
            return order.getUploadedFile().stream()
                .map(ListValue::getValue)
                .map(CICDocument::getDocumentLink)
                .anyMatch(removedDocuments::contains);
        }

        return false;
    }
}
