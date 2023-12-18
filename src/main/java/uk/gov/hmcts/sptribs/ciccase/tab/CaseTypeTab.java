package uk.gov.hmcts.sptribs.ciccase.tab;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.AC_CASEFLAGS_VIEWER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_CONTACT_PREFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_PHONE_NUMBER;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_CATEGORY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_CICA_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_CLAIM_LINKED;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_RECEIVED_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_REGION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_STATE_LABEL;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_SUBCATEGORY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_ALWAYS_HIDE_STAY_REASON;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_APPLICANT_FULL_NAME_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_HEARING_LIST_NOT_ANY_AND_CANCELLATION_REASON_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_HEARING_LIST_NOT_ANY_AND_CASE_POSTPONE_REASON_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_IS_CASE_STAYED;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_IS_NOT_CASE_STAYED;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_REPRESENTATIVE_FULL_NAME_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.COND_REPRESENTATIVE_NOT_EMPTY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.IS_STAYED;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REMOVE_STAY_ADDITIONAL_DETAIL;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REMOVE_STAY_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REMOVE_STAY_OTHER_DESCRIPTION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REMOVE_STAY_REASON;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_ADDRESS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_CONTACT_PREFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_FULLNAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_ORG;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_PHONE_NUMBER;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_PRESENT;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_QUALIFIED;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SCHEME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.STAY_ADDITIONAL_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.STAY_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.STAY_EXPIRATION_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.STAY_FLAG_TYPE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.STAY_REASON;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_DATE_OF_BIRTH;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_PHONE_NUMBER;

@Component
@Setter
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.case-file-view.enabled}")
    private boolean caseFileViewEnabled;

    @Value("${feature.case-flags.enabled}")
    private boolean caseFlagsEnabled;

    @Value("${feature.link-case.enabled}")
    private boolean caseLinkEnabled;

    @Value("${feature.bundling.enabled}")
    private boolean bundlingEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildSummaryTab(configBuilder);
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
        buildCaseReferralTab(configBuilder);
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
            .field(CaseData::getCaseNameHmctsInternal, COND_ALWAYS_HIDE_STAY_REASON, null)
            .field(CaseData::getCaseLinks, "LinkedCasesComponentLauncher!=\"\"", "#ARGUMENT(LinkedCases)");
    }

    private void doBuildCaseFlagTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseFlags", "Case Flags")
            .forRoles(AC_CASEFLAGS_VIEWER, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .field(CaseData::getFlagLauncher, null, "#ARGUMENT(READ)")
            .field(CaseData::getCaseFlags, COND_ALWAYS_HIDE_STAY_REASON)
            .field(CaseData::getSubjectFlags, COND_ALWAYS_HIDE_STAY_REASON)
            .field(CaseData::getApplicantFlags, COND_ALWAYS_HIDE_STAY_REASON)
            .field(CaseData::getRepresentativeFlags, COND_ALWAYS_HIDE_STAY_REASON);
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
            .label(CASE_STATE_LABEL, null, "#### Case Status:  ${[STATE]}")
            .label(CASE_DETAILS, null, "### Case details")
            .field(SUBJECT_NAME)
            .field(SUBJECT_DATE_OF_BIRTH)
            .field(SUBJECT_EMAIL)
            .field(CaseData::getHyphenatedCaseRef)
            .label(REPRESENTATIVE_DETAILS, COND_REPRESENTATIVE_NOT_EMPTY, "### Representative Details")
            .field(REPRESENTATIVE_QUALIFIED)
            .field(REPRESENTATIVE_ORG)
            .field(REPRESENTATIVE_FULLNAME)
            .field(REPRESENTATIVE_PHONE_NUMBER)
            .field(REPRESENTATIVE_EMAIL)
            .field(REPRESENTATIVE_REFERENCE)
            .field(REPRESENTATIVE_PRESENT)
            .label(STAY_DETAILS, "stayStayReason!=\"\" AND stayIsCaseStayed=\"Yes\"", "### Stay Details")
            .field(IS_STAYED, COND_ALWAYS_HIDE_STAY_REASON)
            .field(STAY_REASON, COND_IS_CASE_STAYED)
            .field(STAY_EXPIRATION_DATE, COND_IS_CASE_STAYED)
            .field(STAY_ADDITIONAL_DETAILS, COND_IS_CASE_STAYED)
            .field(STAY_FLAG_TYPE, COND_IS_CASE_STAYED)
            .label(REMOVE_STAY_DETAILS, "removeStayStayRemoveReason!=\"\" AND stayIsCaseStayed=\"No\"", "### Remove Stay Details")
            .field(REMOVE_STAY_REASON, COND_IS_NOT_CASE_STAYED)
            .field(REMOVE_STAY_OTHER_DESCRIPTION, COND_IS_NOT_CASE_STAYED)
            .field(REMOVE_STAY_ADDITIONAL_DETAIL, COND_IS_NOT_CASE_STAYED);
    }

    private void buildStateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("state", "State")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label(CASE_STATE_LABEL, null, "#### Case State:  ${[STATE]}");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, SUPER_USER)
            .field(CaseData::getNotes);
    }

    private void buildBundlesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (bundlingEnabled) {
            doBuildBundlesTab(configBuilder);
        }
    }

    private void doBuildBundlesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("bundles", "Bundles")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .field(CaseData::getCaseBundles);
    }

    private void buildMessagesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("messages", "Messages")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .field(CaseData::getMessages);
    }

    private void buildCaseDetailsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseDetails", "Case Details")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label(CASE_DETAILS, null, "### Case details")
            .field(CASE_CATEGORY)
            .field(CASE_RECEIVED_DATE)
            .field(CASE_SUBCATEGORY)
            .field(SCHEME)
            .field(CASE_REGION)
            .field(CASE_CLAIM_LINKED)
            .field(CASE_CICA_REFERENCE, "cicCaseClaimLinkedToCic = \"Yes\"")
            .field("cicCaseCompensationClaimLinkCIC")
            .field("cicCaseFormReceivedInTime")
            .field("cicCaseMissedTheDeadLineCic", "cicCaseFormReceivedInTime = \"No\"")
            .label("objectSubjects", null, "### Subject Details")
            .field(SUBJECT_NAME)
            .field(SUBJECT_DATE_OF_BIRTH)
            .field(SUBJECT_EMAIL)
            .field(SUBJECT_PHONE_NUMBER)
            .field(SUBJECT_ADDRESS)
            .field("cicCaseSubjectCIC")
            .label(APPLICANT_DETAILS, COND_REPRESENTATIVE_NOT_EMPTY, "### Representative Details")
            .field("cicCaseRepresentativeCIC")
            .field(REPRESENTATIVE_FULLNAME)
            .field(REPRESENTATIVE_ORG)
            .field(REPRESENTATIVE_ADDRESS)
            .field(REPRESENTATIVE_PHONE_NUMBER)
            .field(REPRESENTATIVE_EMAIL)
            .field(REPRESENTATIVE_REFERENCE)
            .field(REPRESENTATIVE_QUALIFIED, COND_REPRESENTATIVE_FULL_NAME_NOT_EMPTY)
            .field(REPRESENTATIVE_CONTACT_PREFERENCE, COND_REPRESENTATIVE_FULL_NAME_NOT_EMPTY)
            .label(APPLICANT_DETAILS, COND_APPLICANT_FULL_NAME_NOT_EMPTY, "### Applicant Details")
            .field(APPLICANT_NAME)
            .field(APPLICANT_DATE_OF_BIRTH, COND_APPLICANT_FULL_NAME_NOT_EMPTY)
            .field(APPLICANT_PHONE_NUMBER)
            .field(APPLICANT_CONTACT_PREFERENCE, COND_APPLICANT_FULL_NAME_NOT_EMPTY)
            .field(APPLICANT_EMAIL)
            .field(APPLICANT_ADDRESS);
    }

    private void buildCasePartiesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseParties", "Case Parties")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("Subject's details", null, "### Subject's details")
            .field(SUBJECT_NAME)
            .field(SUBJECT_EMAIL)
            .field(SUBJECT_PHONE_NUMBER)
            .field(SUBJECT_DATE_OF_BIRTH)
            .field("cicCaseContactPreferenceType")
            .field(SUBJECT_ADDRESS)
            .label("Applicant's details", COND_APPLICANT_FULL_NAME_NOT_EMPTY, "### Applicant's details")
            .field(APPLICANT_NAME)
            .field(APPLICANT_EMAIL)
            .field(APPLICANT_PHONE_NUMBER)
            .field(APPLICANT_DATE_OF_BIRTH, COND_APPLICANT_FULL_NAME_NOT_EMPTY)
            .field(APPLICANT_CONTACT_PREFERENCE, COND_APPLICANT_FULL_NAME_NOT_EMPTY)
            .field(APPLICANT_ADDRESS)
            .label("Representative's details", COND_REPRESENTATIVE_NOT_EMPTY, "### Representative's details")
            .field(REPRESENTATIVE_FULLNAME)
            .field(REPRESENTATIVE_ORG)
            .field(REPRESENTATIVE_ADDRESS)
            .field(REPRESENTATIVE_PHONE_NUMBER)
            .field(REPRESENTATIVE_EMAIL)
            .field(REPRESENTATIVE_REFERENCE)
            .field(REPRESENTATIVE_QUALIFIED, COND_REPRESENTATIVE_FULL_NAME_NOT_EMPTY)
            .field(REPRESENTATIVE_CONTACT_PREFERENCE, COND_REPRESENTATIVE_FULL_NAME_NOT_EMPTY)
            .field(REPRESENTATIVE_ADDRESS)
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
            .label(CASE_STATE_LABEL, null, "#### Case Status: ${[STATE]}")
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
            .label("Listing details", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY, "#### Listing details")
            .field("hearingStatus", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("hearingType", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("hearingFormat", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("hearingVenueNameAndAddress", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("roomAtVenue", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("date", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("session", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("hearingTime", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("videoCallLink", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("importantInfoDetails", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)
            .field("cicCaseHearingNotificationParties", COND_HEARING_LIST_NOT_ANY_AND_HEARING_TYPE_NOT_EMPTY)

            .label("Hearing summary", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY, "#### Hearing summary")
            .field("judge", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("isFullPanel", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("memberList", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("roles", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("others", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("outcome", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("recFile", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)
            .field("recDesc", COND_HEARING_LIST_NOT_ANY_AND_IS_FULL_PANEL_NOT_EMPTY)

            .label("Postponement summary", COND_HEARING_LIST_NOT_ANY_AND_CASE_POSTPONE_REASON_NOT_EMPTY, "#### Postponement summary")
            .field("cicCasePostponeReason", COND_HEARING_LIST_NOT_ANY_AND_CASE_POSTPONE_REASON_NOT_EMPTY)
            .field("cicCasePostponeAdditionalInformation", COND_HEARING_LIST_NOT_ANY_AND_CASE_POSTPONE_REASON_NOT_EMPTY)

            .label("Cancellation summary", COND_HEARING_LIST_NOT_ANY_AND_CANCELLATION_REASON_NOT_EMPTY, "#### Cancellation summary")
            .field("cicCaseHearingCancellationReason", COND_HEARING_LIST_NOT_ANY_AND_CANCELLATION_REASON_NOT_EMPTY)
            .field("cicCaseCancelHearingAdditionalDetail", COND_HEARING_LIST_NOT_ANY_AND_CANCELLATION_REASON_NOT_EMPTY)

            .label("Listing details", null, "#### Listing details")

            .field(CaseData::getHearingList)

            .label("Hearing options",
                "hearingVenues!=\"\" OR roomAtVenue!=\"\" OR addlInstr!=\"\" OR hearingFormat!=\"\" OR shortNotice!=\"\"",
                "#### Hearing summary")
            .field("hearingVenues")
            .field("roomAtVenue")
            .field("addlInstr")
            .field("hearingFormat")
            .field("shortNotice")

            .field(CaseData::getHearingList);
    }

    private void buildCaseReferralTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseReferrals", "Case Referrals")
            .forRoles(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_RESPONDENT, SUPER_USER)
            .label("Referral to Judge", null, "#### Referral to Judge")
            .field("referToJudgeReferralReason")
            .field("referToJudgeReasonForReferral")
            .field("referToJudgeAdditionalInformation")
            .field("referToJudgeReferralDate")
            .label("Referral to Legal Officer", null, "#### Referral to Legal Officer")
            .field("referToLegalOfficerReferralReason")
            .field("referToLegalOfficerReasonForReferral")
            .field("referToLegalOfficerAdditionalInformation")
            .field("referToLegalOfficerReferralDate");
    }
}
