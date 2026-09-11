package uk.gov.hmcts.sptribs.document.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseDocumentType {

    APPLICATION("APPLICATION", "Application"),
    DOCUMENT_MANAGEMENT("DOCUMENT_MANAGEMENT", "Document management"),
    ORDER("ORDER", "Order"),
    DRAFT_ORDER("DRAFT_ORDER", "Draft order"),
    DECISION("DECISION", "Decision"),
    FINAL_DECISION("FINAL_DECISION", "Final decision"),
    HEARING_RECORD("HEARING_RECORD", "Hearing record"),
    CORRESPONDENCE("CORRESPONDENCE", "Correspondence"),
    BUNDLE("BUNDLE", "Bundle"),
    OTHER("OTHER", "Other");

    private final String code;
    private final String label;
}
