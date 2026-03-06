package uk.gov.hmcts.sptribs.common.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.dtos.OrdersListRestoreCandidate;
import uk.gov.hmcts.sptribs.common.dtos.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersListRestoreServiceTest {

    @Mock
    private CaseEventRepository caseEventRepository;

    @InjectMocks
    private OrdersListRestoreService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldIdentifyRemovedOrdersListEntries() {
        String beforeJson = """
                [
                  {"id": "1", "value": {"orderRef": "ORD-001"}},
                  {"id": "2", "value": {"orderRef": "ORD-002"}},
                  {"id": "3", "value": {"orderRef": "ORD-003"}}
                ]
                """;

        String afterJson = """
                [
                  {"id": "1", "value": {"orderRef": "ORD-001"}}
                ]
                """;

        when(caseEventRepository.getRemoveEventsWithPrecedingData(any(), any(), any()))
                .thenReturn(List.of(RemoveEventWithPrecedingData.builder()
                        .caseDataId(123L)
                        .removeEventDate(LocalDateTime.now())
                        .ordersBeforeJson(beforeJson)
                        .ordersAfterJson(afterJson)
                        .build()));

        List<OrdersListRestoreCandidate> results =
                service.findCasesRequiringRestore("caseworker-remove-document",
                        LocalDate.of(2026, 2, 24),
                        LocalDate.of(2026, 3, 5));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRemovedEntries()).hasSize(2);
        assertThat(results.get(0).getRemovedEntries())
                .extracting(node -> node.path("value").path("orderRef").asText())
                .containsExactlyInAnyOrder("ORD-002", "ORD-003");
    }

    @Test
    void shouldReturnEmptyWhenNothingWasRemoved() {
        String json = """
                [{"id": "1", "value": {"orderRef": "ORD-001"}}]
                """;

        when(caseEventRepository.getRemoveEventsWithPrecedingData(any(), any(), any()))
                .thenReturn(List.of(RemoveEventWithPrecedingData.builder()
                        .caseDataId(456L)
                        .removeEventDate(LocalDateTime.now())
                        .ordersBeforeJson(json)
                        .ordersAfterJson(json)
                        .build()));

        List<OrdersListRestoreCandidate> results =
                service.findCasesRequiringRestore("caseworker-remove-document",
                        LocalDate.of(2026, 2, 24),
                        LocalDate.of(2026, 3, 5));

        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleNullPrecedingEventGracefully() {
        when(caseEventRepository.getRemoveEventsWithPrecedingData(any(), any(), any()))
                .thenReturn(List.of(RemoveEventWithPrecedingData.builder()
                        .caseDataId(789L)
                        .removeEventDate(LocalDateTime.now())
                        .ordersBeforeJson(null)
                        .ordersAfterJson("[{\"id\": \"1\"}]")
                        .build()));

        List<OrdersListRestoreCandidate> results =
                service.findCasesRequiringRestore("caseworker-remove-document",
                        LocalDate.of(2026, 2, 24),
                        LocalDate.of(2026, 3, 5));

        // nothing in before means nothing was "removed", so filtered out
        assertThat(results).isEmpty();
    }
}