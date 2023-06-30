package uk.gov.hmcts.sptribs.document.bundling.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static java.util.Objects.requireNonNull;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Callback {

    @JsonProperty("event_id")
    private String event;

    private CaseDetails<CaseData, State> caseDetails;
    private CaseDetails<CaseData, State> caseDetailsBefore;
    private String pageId = null;

    @JsonProperty("ignore_warning")
    private boolean ignoreWarnings;

    private Callback() {
        // noop -- for deserializer
    }

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

    public String getEvent() {
        return event;
    }

    public CaseDetails<CaseData, State> getCaseDetails() {


        return caseDetails;
    }

    public CaseDetails<CaseData, State> getCaseDetailsBefore() {
        return caseDetailsBefore;
    }

    public boolean isIgnoreWarnings() {
        return ignoreWarnings;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
}
