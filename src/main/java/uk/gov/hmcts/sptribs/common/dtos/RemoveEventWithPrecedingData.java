package uk.gov.hmcts.sptribs.common.dtos;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.LocalDateTime;

@Data
@Builder
public class RemoveEventWithPrecedingData {
    private Long caseDataId;
    private Long reference;
    private String currentEvent;
    private LocalDateTime currentEventDate;
    private CaseData currentEventData;
    private String precedingEventId;
    private LocalDateTime precedingEventDate;
    private CaseData precedingEventData;
}
