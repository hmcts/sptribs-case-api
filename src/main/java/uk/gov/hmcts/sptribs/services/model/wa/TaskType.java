package uk.gov.hmcts.sptribs.services.model.wa;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskType {
    processCaseWithdrawalDirections("Process case withdrawal directions"),
    processRule27Decision("Process Rule 27 decision"),
    processListingDirections("Process listing directions"),
    processDirectionsReListedCase("Process directions re. listed case"),
    processDirectionsReListedCaseWithin5Days("Process directions re. listed case (within 5 days)"),
    processSetAsideDirections("Process set aside directions"),
    processCorrections("Process corrections"),
    processDirectionsReturned("Process directions returned"),
    processPostponementDirections("Process postponement directions"),
    processTimeExtensionDirectionsReturned("Process time extension directions returned"),
    processReinstatementDecisionNotice("Process reinstatement decision notice"),
    processOtherDirectionsReturned("Process other directions returned"),
    processWrittenReasons("Process written reasons"),
    processStrikeOutDirectionsReturned("Process strike out directions returned"),
    processStayDirections("Process stay directions"),
    issueDecisionNotice("Issue decision notice"),
    completeHearingOutcome("Complete hearing outcome"),
    issueCaseToRespondent("Issue case to respondent"),
    vetNewCaseDocuments("Vet new case documents"),
    reviewNewCaseAndProvideDirectionsLO("Review new case and provide directions - Legal Officer"),
    reviewTimeExtensionRequestLO("Review time extension request - Legal Officer"),
    reviewStrikeOutRequestLO("Review strike out request - Legal Officer"),
    reviewStayRequestLO("Review stay request - Legal Officer"),
    reviewListingDirectionsLO("Review listing directions - Legal Officer"),
    reviewWithdrawalRequestLO("Review withdrawal request - Legal Officer"),
    reviewRule27RequestLO("Review Rule 27 request - Legal Officer"),
    reviewListCaseLO("Review list case - Legal Officer"),
    reviewOtherRequestLO("Review reinstatement request - Legal Officer"),
    reviewListCaseWithin5DaysLO("Review list case (within 5 days) - Legal Officer"),
    reviewPostponementRequestLO("Review postponement request - Legal Officer"),
    reviewReinstatementRequestLO("Review reinstatement request - Legal Officer"),
    reviewListCaseWithin5DaysJudge("Review list case (within 5 days) - Judge"),
    reviewPostponementRequestJudge("Review postponement request - Judge"),
    reviewCorrectionsRequest("Review corrections request"),
    reviewWrittenReasonsRequest("Review written reasons request"),
    reviewReinstatementRequestJudge("Review reinstatement request - Judge"),
    reviewSetAsideRequest("Review set aside request"),
    reviewStayRequestJudge("Review stay request - Judge"),
    reviewNewCaseAndProvideDirectionsJudge("Review new case and provide directions - Judge"),
    reviewOtherRequestJudge("Review other request - Judge"),
    reviewWithdrawalRequestJudge("Review withdrawal request - Judge"),
    reviewRule27RequestJudge("Review Rule 27 request - Judge"),
    reviewListingDirectionsJudge("Review listing directions - Judge"),
    reviewListCaseJudge("Review list case - Judge"),
    reviewStrikeOutRequestJudge("Review strike out request - Judge"),
    reviewTimeExtensionRequestJudge("Review time extension request - Judge"),
    followUpNoncomplianceOfDirections("Follow up non-compliance of directions"),
    registerNewCase("Register new case"),
    processFurtherEvidence("Process further evidence"),
    stitchCollateHearingBundle("Stitch/ collate hearing bundle"),
    reviewSpecificAccessRequestJudiciary("Review specific access request Judiciary"),
    reviewSpecificAccessRequestLegalOps("Review specific access request Legal Ops"),
    reviewSpecificAccessRequestAdmin("Review specific access request Admin"),
    reviewSpecificAccessRequestCTSC("Review specific access request CTSC"),
    createDueDate("Create due date"),
    issueDueDate("Issue due date");

    private final String name;


}
