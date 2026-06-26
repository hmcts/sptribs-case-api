package uk.gov.hmcts.sptribs.document.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseDocumentType {

    APPLICANT("APPLICANT"),
    RESPONDENT("RESPONDENT"),
    CASEWORKER("CASEWORKER"),
    ORDER("ORDER"),
    DRAFT_ORDER("DRAFT_ORDER"),
    DECISION("DECISION"),
    FINAL_DECISION("FINAL_DECISION"),
    HEARING_RECORD("HEARING_RECORD"),
    CORRESPONDENCE("CORRESPONDENCE"),
    BUNDLE("BUNDLE"),
    OTHER("OTHER");

    private final String code;
}
