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
        label = "Application rejected",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Rejected("Rejected"),

    @CCD(
        label = "Application withdrawn",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    Withdrawn("Withdrawn"),

    @CCD(
        label = "Awaiting applicant 1 response",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant1Response("AwaitingApplicant1Response"),

    @CCD(
        label = "Awaiting applicant 2 response",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant2Response("AwaitingApplicant2Response"),

    @CCD(
        label = "Awaiting conditional order",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingConditionalOrder("AwaitingConditionalOrder"),

    @CCD(
        label = "Draft",
        hint = "### Case record for: ${hyphenatedCaseRef}\n",
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft("Draft"),

    @CCD(
        label = "Submitted",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Submitted("Submitted"),

    @CCD(
        label = "New case received",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCaseReceived("NewCaseReceived"),


    @CCD(
        label = "Case management",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseManagement("CaseManagement"),

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
        label = "Case stayed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseStayed("CaseStayed");

    public static final EnumSet<State> POST_SUBMISSION_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response
    ));

    public static final EnumSet<State> PRE_RETURN_TO_PREVIOUS_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Withdrawn,
        Rejected
    ));

    private final String name;
}

