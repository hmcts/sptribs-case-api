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
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Completed("Completed"),

    @CCD(
        label = "Application rejected",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Rejected("Rejected"),

    @CCD(
        label = "Application Sent",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Sent("Sent"),

    @CCD(
        label = "Application withdrawn",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Withdrawn("Withdrawn"),

    @CCD(
        label = "Awaiting hearing",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHearing("AwaitingHearing"),

    @CCD(
        label = "Awaiting outcome",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingOutcome("AwaitingOutcome"),

    @CCD(
        label = "Case closed",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseClosed("CaseClosed"),

    @CCD(
        label = "Case Concession",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Concession("Concession"),

    @CCD(
        label = "Case management",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseManagement("CaseManagement"),

    @CCD(
        label = "Case stayed",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseStayed("CaseStayed"),

    @CCD(
        label = "Case Strike Out",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    StrikeOut("StrikeOut"),

    @CCD(
        label = "Consent Order",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    ConsentOrder("ConsentOrder"),

    @CCD(
        label = "Draft",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft("Draft"),

    @CCD(
        label = "DSS-Draft",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    DSS_Draft("DSS-Draft"),

    @CCD(
        label = "DSS-Submitted",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    DSS_Submitted("DSS-Submitted"),

    @CCD(
        label = "New case received",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCaseReceived("NewCaseReceived"),

    @CCD(
        label = "Ready to list",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    ReadyToList("ReadyToList"),

    @CCD(
        label = "Rule 27",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Rule27("Rule27"),

    @CCD(
        label = "Submitted",
        hint = "### ${cicCaseFullName}\nCase number: ${hyphenatedCaseRef}",
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

