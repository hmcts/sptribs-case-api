package uk.gov.hmcts.sptribs.ciccase.tab;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;


@Component
@Setter
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.case-file-view.enabled}")
    private boolean caseFileViewEnabled;

    @Value("${feature.case-flags.enabled}")
    private boolean caseFlagsEnabled;

    @Value("${feature.link-case.enabled}")
    private boolean caseLinkEnabled;


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
        buildBundlesTab(configBuilder);
        buildHearing(configBuilder);
        buildCicaDetails(configBuilder);
        buildCaseFileViewTab(configBuilder);
        buildMessagesTab(configBuilder);
        buildCaseFlagTab(configBuilder);
        buildCaseLinkTab(configBuilder);
    }

    private void buildCaseFlagTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseFlagsEnabled) {
            doBuildCaseFlagTab(configBuilder);
        }
    }

    private void buildCaseLinkTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseLinkEnabled) {
            doBuildCaseLinkTab(configBuilder);
        }
    }

    private void doBuildCaseLinkTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseLinks", "Linked cases")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .field(CaseData::getLinkedCasesComponentLauncher, null, "#ARGUMENT(LinkedCases)")
            .field(CaseData::getCaseNameHmctsInternal, null, null)
            .field(CaseData::getCaseLinks, "LinkedCasesComponentLauncher!=\"\"", "#ARGUMENT(LinkedCases)");
    }

    private void doBuildCaseFlagTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseFlags", "Case Flags")
            .field(CaseData::getFlagLauncher, null, "#ARGUMENT(READ)")
            .field(CaseData::getCaseFlags, ALWAYS_HIDE)
            .field(CaseData::getSubjectFlags, ALWAYS_HIDE)
            .field(CaseData::getApplicantFlags, ALWAYS_HIDE)
            .field(CaseData::getRepresentativeFlags, ALWAYS_HIDE);
    }

    private void buildCaseFileViewTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseFileViewEnabled) {
            doBuildCaseFileViewTab(configBuilder);
        }
    }

    private void doBuildCaseFileViewTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseFileView", "Case file view")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .field(CaseData::getCaseFileView1, null, "#ARGUMENT(CaseFileView)");
    }

    private void buildSummaryTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("summary", "Summary")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
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
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, SUPER_USER)
            .field(CaseData::getNotes);
    }

    private void buildMessagesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("messages", "Messages")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .field(CaseData::getMessages);
    }

    private void buildBundlesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("bundles", "Bundles")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .field(CaseData::getCaseBundles);
    }

    private void buildFlagsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {

    }

    private void buildCaseDetailsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseDetails", "Case Details")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("case-details", null, "### Case details")
            .field("cicCaseCaseCategory")
            .field("cicCaseCaseReceivedDate")
            .field("cicCaseCaseSubcategory")
            .label("objectSubjects", null, "### Subject Details")
            .field("cicCaseFullName")
            .field("cicCaseDateOfBirth")
            .field("cicCaseEmail")
            .field("cicCasePhoneNumber")
            .field("cicCaseAddress")
            .field("cicCaseSubjectCIC")
            .label("applicantDetails", "cicCaseRepresentativeFullName!=\"\"", "### Representative Details")
            .field("cicCaseRepresentativeCIC")
            .field("cicCaseRepresentativeFullName")
            .field("cicCaseRepresentativeOrgName")
            .field("cicCaseRepresentativeAddress")
            .field("cicCaseRepresentativePhoneNumber")
            .field("cicCaseRepresentativeEmailAddress")
            .field("cicCaseRepresentativeReference")
            .field("cicCaseIsRepresentativeQualified", "cicCaseRepresentativeFullName!=\"\"")
            .field("cicCaseRepresentativeContactDetailsPreference", "cicCaseRepresentativeFullName!=\"\"")
            .label("applicantDetails", "cicCaseApplicantFullName!=\"\"", "### Applicant Details")
            .field("cicCaseApplicantFullName")
            .field("cicCaseApplicantDateOfBirth", "cicCaseApplicantFullName!=\"\"")
            .field("cicCaseApplicantPhoneNumber")
            .field("cicCaseApplicantContactDetailsPreference", "cicCaseApplicantFullName!=\"\"")
            .field("cicCaseApplicantEmailAddress")
            .field("cicCaseApplicantAddress");
    }

    private void buildCasePartiesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseParties", "Case Parties")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
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
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("Orders", null, "### Orders")
            .label("LabelState", null, "#### Case Status: ${[STATE]}")
            .field("cicCaseDraftOrderCICList")
            .field("cicCaseOrderList")
            .label("Decision", null, "### Decision")
            .field("caseIssueDecisionDecisionDocument")
            .field("caseIssueDecisionIssueDecisionDraft")
            .label("FinalDecision", null, "### Final Decision")
            .field("caseIssueFinalDecisionDocument")
            .field("caseIssueFinalDecisionFinalDecisionDraft", "caseIssueFinalDecisionFinalDecisionDraft!=\"\"");
    }

    private void buildCaseDocumentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseDocuments", "Case Documents")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("Case Documents", null, "#### Case Documents")
            .field("cicCaseApplicantDocumentsUploaded")
            .field("allCaseworkerCICDocument");


    }

    private void buildCicaDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("cicaDetails", "CICA Details")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("CICA Details", null, "#### CICA Details")
            .field(CaseData::getEditCicaCaseDetails);


    }

    private void buildHearing(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("hearings", "Hearings")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("Listing details", null, "#### Listing details")
            .field(CaseData::getHearingList);


    }
}
