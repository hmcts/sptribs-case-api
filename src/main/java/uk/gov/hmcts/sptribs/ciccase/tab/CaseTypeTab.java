package uk.gov.hmcts.sptribs.ciccase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALWAYS_HIDE = "stayStayReason=\"NEVER_SHOW\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildSummaryTab(configBuilder);
        buildFlagsTab(configBuilder);
        buildStateTab(configBuilder);
        buildNotesTab(configBuilder);
        buildCaseDetailsTab(configBuilder);
        buildCasePartiesTab(configBuilder);
        buildOrderTab(configBuilder);
        buildCaseDocumentTab(configBuilder);
        buildHearing(configBuilder);
        buildCicaDetails(configBuilder);
    }


    private void buildSummaryTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("summary", "Summary")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("LabelState", null, "#### Case Status:  ${[STATE]}")
            .label("case-details", null, "### Case details")
            .field("cicCaseFullName")
            .field("cicCaseDateOfBirth")
            .field("cicCaseEmail")
            .field(CaseData::getHyphenatedCaseRef)
            .label("representativeDetails", "cicCaseRepresentativeFullName!=\"\"", "### Representative Details")
            .field("cicCaseIsRepresentativeQualified")
            .field("cicCaseRepresentativeOrgName")
            .field("cicCaseRepresentativeFullName")
            .field("cicCaseRepresentativePhoneNumber")
            .field("cicCaseRepresentativeEmailAddress")
            .field("cicCaseRepresentativeReference")
            .field("cicCaseIsRepresentativePresent")
            .label("stayDetails", "stayStayReason!=\"\" AND stayIsCaseStayed=\"Yes\"", "### Stay Details")
            .field("stayIsCaseStayed", ALWAYS_HIDE)
            .field("stayStayReason", "stayIsCaseStayed=\"Yes\"")
            .field("stayExpirationDate", "stayIsCaseStayed=\"Yes\"")
            .field("stayAdditionalDetail", "stayIsCaseStayed=\"Yes\"")
            .field("stayFlagType", "stayIsCaseStayed=\"Yes\"")
            .label("removeStayDetails", "removeStayStayRemoveReason!=\"\" AND stayIsCaseStayed=\"No\"", "### Remove Stay Details")
            .field("removeStayStayRemoveReason", "stayIsCaseStayed=\"No\"")
            .field("removeStayStayRemoveOtherDescription", "stayIsCaseStayed=\"No\"")
            .field("removeStayAdditionalDetail", "stayIsCaseStayed=\"No\"");

    }

    private void buildStateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("state", "State")
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .field(CaseData::getNotes);
    }

    private void buildFlagsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("flags", "Flags")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("partyLevel", "caseFlagPartyLevelFlags!=\"\"", "Party level flags")
            .field("caseFlagPartyLevelFlags")
            .label("caseLevel", "caseFlagCaseLevelFlags!=\"\"", "Case level flags")
            .field("caseFlagCaseLevelFlags");
    }

    private void buildCaseDetailsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseDetails", "Case Details")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("case-details", null, "### Case details")
            .field("cicCaseCaseCategory")
            .field("cicCaseCaseReceivedDate")
            .field("cicCaseCaseSubcategory")
            .field("cicCaseDateOfBirth")
            .field("cicCaseEmail")
            .field("cicCaseFullName")
            .field("cicCasePhoneNumber")
            .label("objectSubjects", null, "### Object Subjects")
            .field("cicCaseSubjectCIC")
            .field("cicCaseRepresentativeCIC")
            .field("cicCaseRepresentativeFullName")
            .field("cicCaseRepresentativeOrgName")
            .field("cicCaseRepresentativeAddress")
            .field("cicCaseRepresentativePhoneNumber")
            .field("cicCaseRepresentativeEmailAddress")
            .field("cicCaseRepresentativeReference")
            .field("cicCaseIsRepresentativeQualified", "cicCaseRepresentativeFullName!=\"\"")
            .field("cicCaseRepresentativeContactDetailsPreference", "cicCaseRepresentativeFullName!=\"\"")
            .field("cicCaseAddress")
            .label("applicantDetails", null, "### Applicant Details")
            .field("cicCaseApplicantFullName")
            .field("cicCaseApplicantDateOfBirth", "cicCaseApplicantFullName!=\"\"")
            .field("cicCaseApplicantPhoneNumber")
            .field("cicCaseApplicantContactDetailsPreference", "cicCaseApplicantFullName!=\"\"")
            .field("cicCaseApplicantEmailAddress")
            .field("cicCaseApplicantAddress");
    }

    private void buildCasePartiesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseParties", "Case Parties")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("Subject's details", null, "### Subject's details")
            .field("cicCaseFullName")
            .field("cicCaseEmail")
            .field("cicCasePhoneNumber")
            .field("cicCaseDateOfBirth")
            .field("cicCaseContactPreferenceType")
            .field("cicCaseAddress")
            .label("Applicant's details", "cicCaseApplicantFullName!=\"\"", "### Applicant's details")
            .field("cicCaseApplicantFullName")
            .field("cicCaseApplicantEmailAddress")
            .field("cicCaseApplicantPhoneNumber")
            .field("cicCaseApplicantDateOfBirth", "cicCaseApplicantFullName!=\"\"")
            .field("cicCaseApplicantContactDetailsPreference", "cicCaseApplicantFullName!=\"\"")
            .field("cicCaseApplicantAddress")
            .label("Representative's details", "cicCaseRepresentativeFullName!=\"\"", "### Representative's details")
            .field("cicCaseRepresentativeFullName")
            .field("cicCaseRepresentativeOrgName")
            .field("cicCaseRepresentativeAddress")
            .field("cicCaseRepresentativePhoneNumber")
            .field("cicCaseRepresentativeEmailAddress")
            .field("cicCaseRepresentativeReference")
            .field("cicCaseIsRepresentativeQualified", "cicCaseRepresentativeFullName!=\"\"")
            .field("cicCaseRepresentativeContactDetailsPreference", "cicCaseRepresentativeFullName!=\"\"")
            .field("cicCaseRepresentativeAddress")
            .label("Respondent's details", null, "### Respondent's details")
            .field("cicCaseRespondentName")
            .field("cicCaseRespondentOrganisation")
            .field("cicCaseRespondentEmail");


    }

    private void buildOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("orders", "Orders & Decisions")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("Orders", null, "### Orders")
            .label("LabelState", null, "#### Case Status: ${[STATE]}")
            .field("cicCaseDraftOrderCICList")
            .field("cicCaseOrderList")
            .label("Decision", null, "### Decision")
            .field("caseIssueDecisionDecisionDocument", "caseIssueDecisionDecisionDocument!=\"\"")
            .field("caseIssueDecisionIssueDecisionDraft", "caseIssueDecisionIssueDecisionDraft!=\"\"")
            .label("FinalDecision", null, "### Final Decision")
            .field("caseIssueFinalDecisionDocument", "caseIssueFinalDecisionDocument!=\"\"")
            .field("caseIssueFinalDecisionFinalDecisionDraft", "caseIssueFinalDecisionFinalDecisionDraft!=\"\"");
    }

    private void buildCaseDocumentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseDocuments", "Case Documents")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("Case Documents", null, "#### Case Documents")
            .field("cicCaseApplicantDocumentsUploaded");


    }

    private void buildCicaDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("cicaDetails", "CICA Details")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("CICA Details", null, "#### CICA Details")
            .field(CaseData::getEditCicaCaseDetails);


    }

    private void buildHearing(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("hearings", "Hearings")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("Listing details", "hearingType!=\"\"", "#### Listing details")
            .field("hearingStatus")
            .field("hearingType")
            .field("hearingFormat")
            .field("hearingVenueNameAndAddress")
            .field("roomAtVenue")
            .field("date")
            .field("session")
            .field("hearingTime")
            .field("videoCallLink")
            .field("importantInfoDetails")
            .field("cicCaseHearingNotificationParties")

            .label("Hearing summary", "isFullPanel!=\"\"", "#### Hearing summary")
            .field("judge")
            .field("isFullPanel")
            .field("memberList")
            .field("roles")
            .field("others")
            .field("outcome")
            .field("recFile")
            .field("recDesc")
            .label("Postponement summary", "cicCasePostponeReason!=\"\"", "#### Postponement summary")
            .field("cicCasePostponeReason")
            .field("cicCasePostponeAdditionalInformation");


    }
}
