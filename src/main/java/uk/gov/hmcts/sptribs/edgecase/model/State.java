package uk.gov.hmcts.sptribs.edgecase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum State {
    @CCD(
        label = "### Case number: ${[CASE_REFERENCE]}\n ### ${subjectFullName}\n"
    )
    DRAFT("Draft"),

    @CCD(
        label = "### Case number: ${[CASE_REFERENCE]}\n ### ${subjectFullName}\n"
    )
    SUBMITTED("Submitted"),

    @CCD(
        label = "### Case number: ${[CASE_REFERENCE]}\n ### ${subjectFullName}\n"
    )
    AWAITING_PAYMENT("AwaitingPayment");

    private final String name;

}

