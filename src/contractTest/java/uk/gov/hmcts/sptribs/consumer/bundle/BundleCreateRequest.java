package uk.gov.hmcts.sptribs.consumer.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Value
@lombok.Builder(toBuilder = true)
public class BundleCreateRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("caseTypeId")
    private String caseTypeId;
    @com.fasterxml.jackson.annotation.JsonProperty("jurisdictionId")
    private String jurisdictionId;
//    @com.fasterxml.jackson.annotation.JsonProperty("case_details")
//    private BundlingCaseDetails caseDetails;
    @com.fasterxml.jackson.annotation.JsonProperty("event_id")
    private String eventId;

}
