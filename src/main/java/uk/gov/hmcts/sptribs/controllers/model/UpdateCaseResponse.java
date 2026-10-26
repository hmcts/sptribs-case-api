package uk.gov.hmcts.sptribs.controllers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateCaseResponse(@JsonProperty("case_id") String caseId, String state) {
}
