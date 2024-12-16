package uk.gov.hmcts.sptribs.services.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Event {
    SUBMIT("submit"),
    UPDATE("update"),
    UPDATE_CASE("update_case");

    private final String eventType;
}
