package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum TribunalDocuments implements HasLabel {

    @JsonProperty("Application Form")
    APPLICATION_FORM("Application Form", "Application Form"),

    @JsonProperty("First decision")
    FIRST_DECISION("First decision", "First decision"),

    @JsonProperty("Application for review")
    APPLICATION_FOR_REVIEW("Application for review", "Application for review"),

    @JsonProperty("Review decision")
    REVIEW_DECISION("Review decision", "Review decision"),

    @JsonProperty("Notice of Appeal")
    NOTICE_OF_APPEAL("Notice of Appeal", "Notice of Appeal"),

    @JsonProperty("Evidence/correspondence from the Appellant")
    EVIDENCE_CORRESPONDENCE_FROM_THE_APPELLANT("Evidence/correspondence from the Appellant", "Evidence/correspondence from the Appellant"),

    @JsonProperty("Correspondence from the CICA")
    CORRESPONDENCE_FROM_THE_CICA("Correspondence from the CICA", "Correspondence from the CICA");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }
}
