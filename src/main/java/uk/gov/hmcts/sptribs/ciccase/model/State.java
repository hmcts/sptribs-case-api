package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultStateAccessExcludingCAA;
import uk.gov.hmcts.sptribs.ciccase.model.access.LegalAdvisorAccess;
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
        label = "Awaiting bailiff referral",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffReferral("AwaitingBailiffReferral"),

    @CCD(
        label = "Awaiting conditional order",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingConditionalOrder("AwaitingConditionalOrder"),

    @CCD(
        label = "Awaiting joint conditional order",
        hint = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPending("ConditionalOrderPending"),

    @CCD(
        label = "Awaiting judge clarification",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingJudgeClarification("AwaitingJudgeClarification"),

    @CCD(
        label = "Awaiting legal advisor referral",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingLegalAdvisorReferral("AwaitingLegalAdvisorReferral"),

    @CCD(
        label = "Awaiting service",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingService("AwaitingService"),

    @CCD(
        label = "Awaiting service consideration",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingServiceConsideration("AwaitingServiceConsideration"),

    @CCD(
        label = "Awaiting service payment",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingServicePayment("AwaitingServicePayment"),

    @CCD(
        label = "Clarification response submitted",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    ClarificationSubmitted("ClarificationSubmitted"),

    @CCD(
        label = "Conditional order drafted",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderDrafted("ConditionalOrderDrafted"),

    @CCD(
        label = "Conditional order pronounced",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPronounced("ConditionalOrderPronounced"),

    @CCD(
        label = "Conditional order refused",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderRefused("ConditionalOrderRefused"),

    @CCD(
        label = "Draft",
        hint = "### Case record for: ${hyphenatedCaseRef}\n",
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft("Draft"),

    @CCD(
        label = "Final order complete",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    FinalOrderComplete("FinalOrderComplete"),

    @CCD(
        label = "Final order overdue",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    FinalOrderOverdue("FinalOrderOverdue"),

    @CCD(
        label = "Final order pending",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    FinalOrderPending("FinalOrderPending"),

    @CCD(
        label = "Final order requested",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    FinalOrderRequested("FinalOrderRequested"),

    @CCD(
        label = "General application received",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    GeneralApplicationReceived("GeneralApplicationReceived"),

    @CCD(
        label = "General consideration complete",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    GeneralConsiderationComplete("GeneralConsiderationComplete"),

    @CCD(
        label = "Issued to bailiff",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    IssuedToBailiff("IssuedToBailiff"),

    @CCD(
        label = "Listed; awaiting pronouncement",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingPronouncement("AwaitingPronouncement"),

    @CCD(
        label = "New paper case",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    NewPaperCase("NewPaperCase"),

    @CCD(
        label = "Offline document received by CW",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    OfflineDocumentReceived("OfflineDocumentReceived"),

    @CCD(
        label = "Pending hearing outcome",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    PendingHearingOutcome("PendingHearingOutcome"),

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
        AwaitingPronouncement,
        Withdrawn,
        Rejected
    ));

    private final String name;
}

