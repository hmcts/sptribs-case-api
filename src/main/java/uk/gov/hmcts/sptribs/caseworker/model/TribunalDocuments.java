package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

@RequiredArgsConstructor
@Getter
public enum TribunalDocuments implements HasLabel {

    @JsonProperty("Application Form")
    APPLICATION_FORM("Application Form", "Application Form", DocumentType.APPLICATION_FORM),

    @JsonProperty("First decision")
    FIRST_DECISION("First decision", "First decision", DocumentType.FIRST_DECISION),

    @JsonProperty("Application for review")
    APPLICATION_FOR_REVIEW("Application for review", "Application for review", DocumentType.APPLICATION_FOR_REVIEW),

    @JsonProperty("Review decision")
    REVIEW_DECISION("Review decision", "Review decision", DocumentType.REVIEW_DECISION),

    @JsonProperty("Notice of Appeal")
    NOTICE_OF_APPEAL("Notice of Appeal", "Notice of Appeal", DocumentType.NOTICE_OF_APPEAL),

    //@JsonProperty("Evidence/correspondence from the Appellant")
    //EVIDENCE_CORRESPONDENCE_FROM_THE_APPELLANT("Evidence/correspondence from the Appellant",
    // "Evidence/correspondence from the Appellant",DocumentType.EVIDENCE_CORRESPONDENCE_FROM_THE_APPELLANT),

    @JsonProperty("Correspondence from the CICA")
    CORRESPONDENCE_FROM_THE_CICA("Correspondence from the CICA", "Correspondence from the CICA",
        DocumentType.CORRESPONDENCE_FROM_THE_CICA);

    private final String reason;
    private final String label;
    private final DocumentType documentType;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

    public DocumentType getDocumentType() {
        return this.documentType;
    }
}
