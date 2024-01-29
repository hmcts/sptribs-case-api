package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BundleCallback extends Callback {

    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("jurisdictionId")
    private String jurisdictionId;

    public BundleCallback(Callback callback) {
        super(callback.getCaseDetails(), callback.getCaseDetailsBefore(), callback.getEvent(), callback.isIgnoreWarnings());
        setCaseTypeId(callback.getCaseDetails().getCaseTypeId());
        setJurisdictionId(callback.getCaseDetails().getJurisdiction());
    }
}
