package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.dtos.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersListRestoreService {

    private final CaseEventRepository caseEventRepository;

    public CaseData restoreOrdersList(Long reference, CaseData currentData, LocalDate startDate, LocalDate endDate) {

        List<RemoveEventWithPrecedingData> removeEvents = caseEventRepository
            .getRemoveEventsWithPrecedingData(reference, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, startDate, endDate);

        if (removeEvents.isEmpty()) {
            log.info("No remove events found for reference={}, nothing to restore", reference);
            return currentData;
        }

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
            return currentData;
        }

        log.info("Restoring {} ordersList entries for reference={}",
            restoredOrdersList.size(), reference);

        List<ListValue<Order>> mergedOrdersList = Stream.concat(
            currentOrdersList.stream(),
            restoredOrdersList.stream()
        ).toList();

        currentData.getCicCase().setOrderList(mergedOrdersList);
        return currentData;
    }

    private List<ListValue<Order>> findRemovedEntries(RemoveEventWithPrecedingData event) {
        List<ListValue<Order>> before = event.getPrecedingEventData().getCicCase().getOrderList();
        List<ListValue<Order>> after = event.getCurrentEventData().getCicCase().getOrderList();
        return before.stream()
            .filter(entry -> !after.contains(entry))
            .toList();
    }

    private boolean hasOrdersList(CaseData data) {
        return data != null
            && data.getCicCase().getOrderList() != null
            && !data.getCicCase().getOrderList().isEmpty();
    }
}
