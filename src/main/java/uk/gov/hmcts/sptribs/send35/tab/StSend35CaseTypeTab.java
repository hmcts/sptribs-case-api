package uk.gov.hmcts.sptribs.send35.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER;

/**
 * Tabs for the SEND35 case type — the caseworker's view of a submitted appeal.
 *
 * <p>This is what "see all the details in the tabs" means: every answer the citizen gave is
 * readable here, grouped the way a caseworker reads an appeal rather than the way SEND35
 * numbers its sections. Section 3 to 7 collapse into one "Other people involved" tab, and
 * mediation and the time limits sit together because the two dates are what decide whether
 * the appeal is in time.
 *
 * <p>Fields are addressed by CCD field id rather than by getter: the questions live on
 * {@code @JsonUnwrapped} complexes, so the id is {@code <prefix><JsonProperty>} and there is
 * no single getter to reference. Each field carries its own show condition because this SDK
 * version has no tab-level condition.
 */
@Component
public class StSend35CaseTypeTab implements CCDConfig<StSend35CaseData, StSend35State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        buildChildOrYoungPersonTab(configBuilder);
        buildAppellantTab(configBuilder);
        buildOtherPartiesTab(configBuilder);
        buildCommunicationTab(configBuilder);
        buildTypeOfAppealTab(configBuilder);
        buildSchoolOrProviderTab(configBuilder);
        buildReasonsTab(configBuilder);
        buildHealthAndSocialCareTab(configBuilder);
        buildMediationAndTimeLimitsTab(configBuilder);
        buildOtherCasesTab(configBuilder);
        buildHearingAndSupportTab(configBuilder);
        buildSupportingEvidenceTab(configBuilder);
        buildDeclarationTab(configBuilder);
    }

    private void buildChildOrYoungPersonTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("childOrYoungPerson", "Child or young person")
            .forRoles(CASEWORKER)
            .field("cypFirstName")
            .field("cypLastName")
            .field("cypDateOfBirth")
            .field("cypGender");
    }

    private void buildAppellantTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("appellant", "Person appealing")
            .forRoles(CASEWORKER)
            .field("appellantWhoIsAppealing")
            .field("appellantFirstName")
            .field("appellantLastName")
            .field("appellantRelationship")
            .field("appellantPhoneNumber")
            .field("appellantEmailAddress")
            .field("appellantAddress");
    }

    private void buildOtherPartiesTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("otherParties", "Other people involved")
            .forRoles(CASEWORKER)
            .field("parentsAddParentOrCarer")
            .field("parentsFirstName", "parentsAddParentOrCarer=\"Yes\"")
            .field("parentsLastName", "parentsAddParentOrCarer=\"Yes\"")
            .field("parentsPhoneNumber", "parentsAddParentOrCarer=\"Yes\"")
            .field("parentsEmailAddress", "parentsAddParentOrCarer=\"Yes\"")
            .field("parentsRelationship", "parentsAddParentOrCarer=\"Yes\"")
            .field("parentsAddSecondParentOrCarer", "parentsAddParentOrCarer=\"Yes\"")
            .field("parentsSecondFirstName", "parentsAddSecondParentOrCarer=\"Yes\"")
            .field("parentsSecondLastName", "parentsAddSecondParentOrCarer=\"Yes\"")
            .field("parentsSecondPhoneNumber", "parentsAddSecondParentOrCarer=\"Yes\"")
            .field("parentsSecondEmailAddress", "parentsAddSecondParentOrCarer=\"Yes\"")
            .field("parentsSecondRelationship", "parentsAddSecondParentOrCarer=\"Yes\"")
            .field("repHasRepresentative")
            .field("repFirstName", "repHasRepresentative=\"Yes\"")
            .field("repLastName", "repHasRepresentative=\"Yes\"")
            .field("repCompanyName", "repHasRepresentative=\"Yes\"")
            .field("repPhoneNumber", "repHasRepresentative=\"Yes\"")
            .field("repEmailAddress", "repHasRepresentative=\"Yes\"")
            .field("advHasAdvocate")
            .field("advFirstName", "advHasAdvocate=\"Yes\"")
            .field("advLastName", "advHasAdvocate=\"Yes\"")
            .field("advPhoneNumber", "advHasAdvocate=\"Yes\"")
            .field("advEmailAddress", "advHasAdvocate=\"Yes\"")
            .field("prOtherPersonOrOrganisation")
            .field("prName", "prOtherPersonOrOrganisation=\"Yes\"")
            .field("prTold", "prOtherPersonOrOrganisation=\"Yes\"")
            .field("prReasonNotTold", "prTold=\"No\"");
    }

    private void buildCommunicationTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("communication", "Correspondence")
            .forRoles(CASEWORKER)
            .field("commsRecipient")
            .field("commsFirstName")
            .field("commsLastName")
            .field("commsMobileNumber")
            .field("commsEmailAddress")
            .field("commsAddress");
    }

    private void buildTypeOfAppealTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("typeOfAppeal", "Type of appeal")
            .forRoles(CASEWORKER)
            .field("appealAppealAbout")
            .field("appealFollowingAnnualReview")
            .field("appealPlanSections");
    }

    private void buildSchoolOrProviderTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("schoolOrProvider", "School or college")
            .forRoles(CASEWORKER)
            .field("schoolSectionIDisagreement")
            .field("schoolAskedForProvider")
            .field("schoolTypeOfProvider", "schoolAskedForProvider=\"No\"")
            .field("schoolProviderName", "schoolAskedForProvider=\"Yes\"")
            .field("schoolProviderAddress", "schoolAskedForProvider=\"Yes\"")
            .field("schoolDateContacted", "schoolAskedForProvider=\"Yes\"")
            .field("schoolProviderResponse", "schoolAskedForProvider=\"Yes\"");
    }

    private void buildReasonsTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("reasons", "Reasons for the appeal")
            .forRoles(CASEWORKER)
            .field("reasonsAppealReasons");
    }

    private void buildHealthAndSocialCareTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("healthAndSocialCare", "Health and social care")
            .forRoles(CASEWORKER)
            .field("hscWantRecommendation")
            .field("hscRecommendationTypes", "hscWantRecommendation=\"Yes\"")
            .field("hscHealthIssues", "hscWantRecommendation=\"Yes\"")
            .field("hscHealthRecommendations", "hscWantRecommendation=\"Yes\"")
            .field("hscSocialCareIssues", "hscWantRecommendation=\"Yes\"")
            .field("hscSocialCareRecommendations", "hscWantRecommendation=\"Yes\"");
    }

    private void buildMediationAndTimeLimitsTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("mediationAndTimeLimits", "Mediation and time limits")
            .forRoles(CASEWORKER)
            .field("medHasCertificate")
            .field("medNoCertificateReason", "medHasCertificate=\"No\"")
            .field("medNoCertificateExplanation", "medNoCertificateReason=\"otherReason\"")
            .field("timeDecisionLetterDate")
            .field("timeMediationCertificateDate")
            .field("timeLateAppealExplanation");
    }

    private void buildOtherCasesTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("otherCases", "Other cases")
            .forRoles(CASEWORKER)
            .field("otherOtherSendAppeals")
            .field("otherAppealReferenceNumbers", "otherOtherSendAppeals=\"Yes\"")
            .field("otherOtherCourtCases")
            .field("otherCourtCaseDetails", "otherOtherCourtCases=\"Yes\"");
    }

    private void buildHearingAndSupportTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("hearingAndSupport", "Hearing and support")
            .forRoles(CASEWORKER)
            .field("hearingPreferredType")
            .field("hearingWantEarlierHearing")
            .field("hearingCanAttendByVideo")
            .field("hearingCannotAttendByVideoReason", "hearingCanAttendByVideo=\"No\"")
            .field("supportNeedsInterpreter")
            .field("supportLanguages", "supportNeedsInterpreter=\"Yes\"")
            .field("supportNeedsAdjustments")
            .field("supportAdjustmentsDetail", "supportNeedsAdjustments=\"Yes\"");
    }

    private void buildSupportingEvidenceTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("supportingEvidence", "Supporting evidence")
            .forRoles(CASEWORKER)
            .field("SupportingEvidence");
    }

    private void buildDeclarationTab(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.tab("declaration", "Declaration")
            .forRoles(CASEWORKER)
            .field("declCapacity")
            .field("declSignatoryRole")
            .field("declFullName")
            .field("declSignature")
            .field("declDateSigned");
    }
}
