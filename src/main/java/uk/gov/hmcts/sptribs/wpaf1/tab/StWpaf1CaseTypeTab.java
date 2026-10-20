package uk.gov.hmcts.sptribs.wpaf1.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER;

/**
 * Tabs for the WPAF1 case type — the caseworker's view of a submitted appeal.
 *
 * <p>This is what "see all the details in the tabs" means: every answer the citizen gave is
 * readable here, grouped the way a caseworker reads an appeal rather than the way WPAF1
 * numbers its sections. Sections 2 and 3 share a tab because they answer the same question
 * — who else is acting — and Sections 6 and 7 share one because together they are what
 * decides whether the appeal is admitted at all.
 *
 * <p>Fields are addressed by CCD field id rather than by getter: the questions live on
 * {@code @JsonUnwrapped} complexes, so the id is {@code <prefix><JsonProperty>} and there is
 * no single getter to reference. Each field carries its own show condition because this SDK
 * version has no tab-level condition.
 */
@Component
public class StWpaf1CaseTypeTab implements CCDConfig<StWpaf1CaseData, StWpaf1State, UserRole> {

    private static final String HAS_REPRESENTATIVE = "repHasRepresentative=\"Yes\"";
    private static final String HAS_APPOINTEE = "aptHasAppointee=\"Yes\"";

    @Override
    public void configure(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        buildAppellantTab(configBuilder);
        buildRepresentativeAndAppointeeTab(configBuilder);
        buildDecisionTab(configBuilder);
        buildReasonsTab(configBuilder);
        buildReconsiderationAndTimeLimitsTab(configBuilder);
        buildSupportAndAdjustmentsTab(configBuilder);
        buildUksfTab(configBuilder);
        buildSupportingEvidenceTab(configBuilder);
        buildDeclarationTab(configBuilder);
    }

    private void buildAppellantTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("appellant", "Appellant")
            .forRoles(CASEWORKER)
            .field("appTitle")
            .field("appFirstName")
            .field("appLastName")
            .field("appDateOfBirth")
            .field("appNationalInsuranceNumber")
            .field("appAfcsMemberNumber")
            .field("appLivesIn")
            .field("appAddress")
            .field("appPhoneNumber")
            .field("appMobileNumber")
            .field("appEmailAddress")
            .field("appServiceStatus")
            .field("appProposedDischargeDate", "appServiceStatus=\"stillServing\"");
    }

    private void buildRepresentativeAndAppointeeTab(
        final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {

        configBuilder.tab("representativeAndAppointee", "Representative and appointee")
            .forRoles(CASEWORKER)
            .field("repHasRepresentative")
            .field("repFirstName", HAS_REPRESENTATIVE)
            .field("repLastName", HAS_REPRESENTATIVE)
            .field("repOrganisation", HAS_REPRESENTATIVE)
            .field("repAddress", HAS_REPRESENTATIVE)
            .field("repDxNumber", HAS_REPRESENTATIVE)
            .field("repReferenceNumber", HAS_REPRESENTATIVE)
            .field("repPhoneNumber", HAS_REPRESENTATIVE)
            .field("repMobileNumber", HAS_REPRESENTATIVE)
            .field("repEmailAddress", HAS_REPRESENTATIVE)
            .field("aptHasAppointee")
            .field("aptFirstName", HAS_APPOINTEE)
            .field("aptLastName", HAS_APPOINTEE)
            .field("aptOrganisation", HAS_APPOINTEE)
            .field("aptAddress", HAS_APPOINTEE)
            .field("aptPhoneNumber", HAS_APPOINTEE)
            .field("aptMobileNumber", HAS_APPOINTEE)
            .field("aptEmailAddress", HAS_APPOINTEE)
            .field("aptSigningGround", HAS_APPOINTEE);
    }

    private void buildDecisionTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("decision", "Decision appealed")
            .forRoles(CASEWORKER)
            .field("dsnScheme")
            .field("dsnDecisionLetterDate")
            .field("dsnAppealAbout")
            .field("dsnAppealAboutOther", "dsnAppealAboutCONTAINS \"other\"");
    }

    private void buildReasonsTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("reasons", "Reasons for appealing")
            .forRoles(CASEWORKER)
            .field("rsnAppealReasons");
    }

    private void buildReconsiderationAndTimeLimitsTab(
        final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {

        configBuilder.tab("reconsiderationAndTimeLimits", "Reconsideration and time limits")
            .forRoles(CASEWORKER)
            .field("recAskedForReconsideration", "dsnScheme=\"afcs\"")
            .field("timeLateAppealExplanation");
    }

    private void buildSupportAndAdjustmentsTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("supportAndAdjustments", "Support and adjustments")
            .forRoles(CASEWORKER)
            .field("supNeedsSupport")
            .field("supSupportDetails", "supNeedsSupport=\"Yes\"");
    }

    private void buildUksfTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("uksf", "UK Special Forces")
            .forRoles(CASEWORKER)
            .field("uksfServedWithUksf")
            .field("uksfDisclosureCellConfirmed", "uksfServedWithUksf=\"Yes\"");
    }

    private void buildSupportingEvidenceTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("supportingEvidence", "Additional evidence")
            .forRoles(CASEWORKER)
            .field("SupportingEvidence");
    }

    private void buildDeclarationTab(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.tab("declaration", "Declaration")
            .forRoles(CASEWORKER)
            .field("declCapacity")
            .field("declSignatoryRole")
            .field("declFullName")
            .field("declSignature")
            .field("declDateSigned");
    }
}
