package uk.gov.hmcts.sptribs.document.bundling.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static java.util.Objects.requireNonNull;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Callback {

    @JsonProperty("event_id")
    private final String event;

    private final CaseDetails<CaseData, State> caseDetails;
    private final CaseDetails<CaseData, State> caseDetailsBefore;

    @Setter
    private String pageId;

    @JsonProperty("ignore_warning")
    private final boolean ignoreWarnings;

    @JsonCreator
    public Callback(CaseDetails<CaseData, State> caseDetails,
                    CaseDetails<CaseData, State> caseDetailsBefore,
                    String event,
                    boolean ignoreWarnings) {
        requireNonNull(caseDetails);
        requireNonNull(caseDetailsBefore);
        requireNonNull(event);

        this.caseDetails = caseDetails;
        this.caseDetailsBefore = caseDetailsBefore;
        this.event = event;
        this.ignoreWarnings = ignoreWarnings;
    }

}
