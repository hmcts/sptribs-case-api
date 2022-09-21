package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccessExcludingCAA;

@RequiredArgsConstructor
@Getter
public enum NextState implements HasLabel {

    @CCD(
        label = "Case management",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseManagement("Case Management"),
    @CCD(
        label = "New case pending review",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCasePendingReview("New Case Pending Review");


    private String type;
    private final String label;

    public String getName() {
        return this.name();
    }

}
