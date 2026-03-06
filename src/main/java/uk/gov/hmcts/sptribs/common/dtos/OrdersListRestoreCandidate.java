package uk.gov.hmcts.sptribs.common.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdersListRestoreCandidate {
    private Long caseDataId;
    private LocalDateTime removeEventDate;
    private List<JsonNode> ordersBefore;
    private List<JsonNode> ordersAfter;
    private List<JsonNode> removedEntries;
}
