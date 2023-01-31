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
            .field("cicCaseIsRepresentativePresent")
            .label("representativeDetails", "cicCaseRepresentativeFullName!=\"\"", "### Representative Details")
            .field("cicCaseIsRepresentativeQualified")
            .field("cicCaseRepresentativeOrgName")
            .field("cicCaseRepresentativeFullName")
            .field("cicCaseRepresentativePhoneNumber")
            .field("cicCaseRepresentativeEmailAddress")
            .field("cicCaseRepresentativeReference");
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
            .field("cicCaseIsRepresentativeQualified")
            .field("cicCaseRepresentativeContactDetailsPreference")
            .field("cicCaseAddress")
            .label("applicantDetails", null, "### Applicant Details")
            .field("cicCaseApplicantFullName")
            .field("cicCaseApplicantDateOfBirth")
            .field("cicCaseApplicantPhoneNumber")
            .field("cicCaseApplicantContactDetailsPreference")
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
            .field("cicCaseApplicantDateOfBirth")
            .field("cicCaseApplicantContactDetailsPreference")
            .field("cicCaseApplicantAddress")
            .label("Representative's details", "cicCaseRepresentativeFullName!=\"\"", "### Representative's details")
            .field("cicCaseRepresentativeFullName")
            .field("cicCaseRepresentativeOrgName")
            .field("cicCaseRepresentativeAddress")
            .field("cicCaseRepresentativePhoneNumber")
            .field("cicCaseRepresentativeEmailAddress")
            .field("cicCaseRepresentativeReference")
            .field("cicCaseIsRepresentativeQualified")
            .field("cicCaseRepresentativeContactDetailsPreference")
            .field("cicCaseRepresentativeAddress")
            .label("Respondant's details", null, "### Respondant's details")
            .field("cicCaseRespondantName")
            .field("cicCaseRespondantOrganisation")
            .field("cicCaseRespondantEmail");


    }

    private void buildOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("orders", "Orders")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("Orders", null, "### Orders")
            .label("LabelState", null, "#### Case Status: ${[STATE]}")
            .field("cicCaseDraftOrderCICList");


    }

    private void buildCaseDocumentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("caseDocuments", "Case Documents")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("Case Documents", null, "#### Case Documents")
            .field("cicCaseApplicantDocumentsUploaded");


    }
}
