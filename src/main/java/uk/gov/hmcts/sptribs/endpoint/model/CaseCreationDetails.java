package uk.gov.hmcts.sptribs.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class CaseCreationDetails {

    @JsonProperty("case_type_id")
    private final String caseTypeId;

    @JsonProperty("event_id")
    private final String eventId;

    @JsonProperty("case_data")
    private final Map<String, Object> caseData;

    @JsonCreator
    public CaseCreationDetails(
        @JsonProperty("case_type_id") String caseTypeId,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("case_data") Map<String, Object> caseData
    ) {
        this.caseTypeId = caseTypeId;
        this.eventId = eventId;
        this.caseData = caseData;
    }
}
