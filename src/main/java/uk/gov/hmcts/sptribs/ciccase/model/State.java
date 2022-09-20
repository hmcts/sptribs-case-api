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
        label = "20 week holding period",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}

    )
    Holding("Holding"),

    @CCD(
        label = "AoS awaiting",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingAos("AwaitingAos"),

    @CCD(
        label = "AoS drafted",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AosDrafted("AosDrafted"),

    @CCD(
        label = "AoS overdue",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AosOverdue("AosOverdue"),

    @CCD(
        label = "Applicant 2 approved",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    Applicant2Approved("Applicant2Approved"),

    @CCD(
        label = "Application awaiting payment",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingPayment("AwaitingPayment"),

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
        label = "Awaiting admin clarification",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingAdminClarification("AwaitingAdminClarification"),

    @CCD(
        label = "Awaiting alternative service",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingAlternativeService("AwaitingAlternativeService"),

    @CCD(
        label = "Awaiting amended application",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingAmendedApplication("AwaitingAmendedApplication"),

    @CCD(
        label = "Awaiting applicant",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingDocuments("AwaitingDocuments"),

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
        label = "Awaiting bailiff service",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffService("AwaitingBailiffService"),

    @CCD(
        label = "Awaiting clarification",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingClarification("AwaitingClarification"),

    @CCD(
        label = "Awaiting conditional order",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingConditionalOrder("AwaitingConditionalOrder"),

    @CCD(
        label = "Awaiting DWP response",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingDwpResponse("AwaitingDWPResponse"),

    @CCD(
        label = "Awaiting final order",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingFinalOrder("AwaitingFinalOrder"),

    @CCD(
        label = "Awaiting general consideration",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralConsideration("AwaitingGeneralConsideration"),

    @CCD(
        label = "Awaiting general referral payment",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralReferralPayment("AwaitingGeneralReferralPayment"),

    @CCD(
        label = "Awaiting HWF decision",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFDecision("AwaitingHWFDecision"),

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
        label = "Removed from bulk case",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccess.class}
    )
    BulkCaseReject("BulkCaseReject"),

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
        label = "New case pending review",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCasePendingReview("NewCasePendingReview"),


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
        label = "New case closed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCaseClosed("NewCaseClosed"),


    @CCD(
        label = "Case closed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    CaseClosed("CaseClosed"),


    @CCD(
        label = "New case stayed",
        hint = "### Case number: ${hyphenatedCaseRef}",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    NewCaseStayed("NewCaseStayed"),


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
        Applicant2Approved,
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved
    ));

    public static final EnumSet<State> STATES_NOT_WITHDRAWN_OR_REJECTED = EnumSet.complementOf(EnumSet.of(
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> PRE_RETURN_TO_PREVIOUS_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPronouncement,
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> POST_ISSUE_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        AwaitingHWFDecision,
        AwaitingDocuments,
        Submitted,
        Withdrawn,
        Rejected
    ));

    public static final State[] AOS_STATES = {
        Holding, AwaitingConditionalOrder, IssuedToBailiff, AwaitingBailiffService, AwaitingBailiffReferral,
        AwaitingServiceConsideration, AwaitingServicePayment, AwaitingAlternativeService, AwaitingDwpResponse,
        AwaitingJudgeClarification, GeneralConsiderationComplete, AwaitingGeneralReferralPayment, AwaitingGeneralConsideration,
        GeneralApplicationReceived, PendingHearingOutcome
    };

    private final String name;

}

