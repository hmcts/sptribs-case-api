package uk.gov.hmcts.sptribs.document.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseDocumentType {

    APPLICATION("APPLICATION"),
    EVIDENCE("EVIDENCE"),
    CORRESPONDENCE("CORRESPONDENCE"),
    TRIBUNAL_DOCUMENT("TRIBUNAL_DOCUMENT"),
    HEARING_RECORD("HEARING_RECORD"),
    BUNDLE("BUNDLE");

    private final String code;
}
