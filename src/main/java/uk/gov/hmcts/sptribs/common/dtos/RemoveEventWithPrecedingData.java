package uk.gov.hmcts.sptribs.common.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

//TODO think about where to place these DTO classes
@Data
@Builder
public class RemoveEventWithPrecedingData {
    private Long caseDataId;
    private LocalDateTime removeEventDate;
    private String ordersAfterJson;   // raw jsonb string from remove event
    private String ordersBeforeJson;  // raw jsonb string from preceding event, nullable
}
