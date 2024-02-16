package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccessExcludingCAA;
import uk.gov.hmcts.sptribs.ciccase.model.access.SolicitorAccess;

import java.util.EnumSet;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        label = "Application completed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Completed("Completed"),

    @CCD(
        label = "Application rejected",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Rejected("Rejected"),

    @CCD(
        label = "Application Sent",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Sent("Sent"),

    @CCD(
        label = "Application withdrawn",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Withdrawn("Withdrawn"),

    @CCD(
        label = "Awaiting hearing",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHearing("AwaitingHearing"),

    @CCD(
        label = "Awaiting outcome",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingOutcome("AwaitingOutcome"),

    @CCD(
        label = "Case closed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseClosed("CaseClosed"),

    @CCD(
        label = "Case Concession",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Concession("Concession"),

    @CCD(
        label = "Case management",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseManagement("CaseManagement"),

    @CCD(
        label = "Case stayed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseStayed("CaseStayed"),

    @CCD(
        label = "Case Strike Out",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    StrikeOut("StrikeOut"),

    @CCD(
        label = "Consent Order",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    ConsentOrder("ConsentOrder"),

    @CCD(
        label = "Draft",
        hint = "### Case record for: ${hyphenatedCaseRef}\n",
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft("Draft"),

    @CCD(
        label = "DSS-Draft",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    DSS_Draft("DSS-Draft"),

    @CCD(
        label = "DSS-Submitted",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    DSS_Submitted("DSS-Submitted"),

    @CCD(
        label = "New case received",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCaseReceived("NewCaseReceived"),

    @CCD(
        label = "Ready to list",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    ReadyToList("ReadyToList"),

    @CCD(
        label = "Rule 27",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Rule27("Rule27"),

    @CCD(
        label = "Submitted",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Submitted("Submitted");

    public static final EnumSet<State> POST_SUBMISSION_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED = EnumSet.complementOf(EnumSet.of(
        Draft
    ));

    public static final EnumSet<State> BUNDLE_STATES = EnumSet.of(
        CaseManagement,
        AwaitingHearing
    );
    private final String name;
}

