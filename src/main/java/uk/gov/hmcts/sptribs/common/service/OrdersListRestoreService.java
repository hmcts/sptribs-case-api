package uk.gov.hmcts.sptribs.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.dtos.OrdersListRestoreCandidate;
import uk.gov.hmcts.sptribs.common.dtos.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersListRestoreService {

    private final CaseEventRepository caseEventRepository;
    private final ObjectMapper objectMapper;

    public List<OrdersListRestoreCandidate> findCasesRequiringRestore(
            String caseEventId, LocalDate startDate, LocalDate endDate) {

        return caseEventRepository
                .getRemoveEventsWithPrecedingData(caseEventId, startDate, endDate)
                .stream()
                .map(this::buildRestoreCandidate)
                .filter(candidate -> !candidate.getRemovedEntries().isEmpty())
                .peek(candidate -> log.info(
                        "Case {} has {} removed ordersList entries to restore",
                        candidate.getCaseDataId(),
                        candidate.getRemovedEntries().size()))
                .toList();
    }

    private OrdersListRestoreCandidate buildRestoreCandidate(RemoveEventWithPrecedingData event) {
        List<JsonNode> before = parseOrdersList(event.getOrdersBeforeJson(), event.getCaseDataId());
        List<JsonNode> after = parseOrdersList(event.getOrdersAfterJson(), event.getCaseDataId());

        return OrdersListRestoreCandidate.builder()
                .caseDataId(event.getCaseDataId())
                .removeEventDate(event.getRemoveEventDate())
                .ordersBefore(before)
                .ordersAfter(after)
                .removedEntries(findRemovedEntries(before, after))
                .build();
    }

    private List<JsonNode> parseOrdersList(String json, Long caseDataId) {
        if (json == null) {
            log.warn("Null ordersList JSON for case {}, treating as empty", caseDataId);
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                log.warn("ordersList for case {} is not an array, treating as empty", caseDataId);
                return List.of();
            }
            List<JsonNode> entries = new ArrayList<>();
            node.forEach(entries::add);
            return entries;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ordersList JSON for case {}", caseDataId, e);
            return List.of();
        }
    }

    private List<JsonNode> findRemovedEntries(List<JsonNode> before, List<JsonNode> after) {
        return before.stream()
                .filter(entry -> !after.contains(entry))  // JsonNode.equals() is deep
                .toList();
    }
}
