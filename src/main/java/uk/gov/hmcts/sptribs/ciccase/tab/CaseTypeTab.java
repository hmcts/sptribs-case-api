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

    private static final String IS_JOINT = "applicationType=\"jointApplication\"";
    private static final String IS_JOINT_AND_HWF_ENTERED = "applicationType=\"jointApplication\" AND applicant2HWFReferenceNumber=\"*\"";
    private static final String IS_NEW_PAPER_CASE = "newPaperCase=\"Yes\"";
    private static final String PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS =
        "paperFormApplicant1NoPaymentIncluded=\"Yes\" AND paperFormSoleOrApplicant1PaymentOther=\"Yes\"";
    private static final String PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS =
        "paperFormApplicant2NoPaymentIncluded=\"Yes\" AND paperFormApplicant2PaymentOther=\"Yes\"";
    private static final String PAPER_FORM_PAYMENT_OTHER_DETAILS =
        String.format("(%s) OR (%s)", PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS, PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS);

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildSummaryTab(configBuilder);
        buildFlagsTab(configBuilder);
        buildStateTab(configBuilder);
        buildPaymentTab(configBuilder);
        buildDocumentsTab(configBuilder);
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
            //.forRoles(APPLICANT_2_SOLICITOR)
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    private void buildPaymentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .forRoles(COURT_ADMIN_CIC, SUPER_USER)
            .label("LabelApplicant1-PaymentHeading", IS_JOINT, "### The applicant")
            .field("applicant1HWFReferenceNumber")
            .label("LabelApplicant2-PaymentHeading", IS_JOINT_AND_HWF_ENTERED, "### ${labelContentTheApplicant2UC}")
            .field("applicant2HWFReferenceNumber", IS_JOINT_AND_HWF_ENTERED)
            .field("newPaperCase", "applicationType=\"NEVER_SHOW\"")
            .label("LabelPaperCase-PaymentHeading", IS_NEW_PAPER_CASE, "### Paper Case Payment")
            .field("paperCasePaymentMethod", IS_NEW_PAPER_CASE)
            .field("paperFormApplicant1NoPaymentIncluded", "applicationType=\"NEVER_SHOW\"")
            .field("paperFormApplicant2NoPaymentIncluded", "applicationType=\"NEVER_SHOW\"")
            .field("paperFormSoleOrApplicant1PaymentOther", "applicationType=\"NEVER_SHOW\"")
            .field("paperFormApplicant2PaymentOther", "applicationType=\"NEVER_SHOW\"")
            .label("LabelPaperForm-App1PaymentHeading", PAPER_FORM_PAYMENT_OTHER_DETAILS, "### Paper Form Payment Details")
            .field("paperFormSoleOrApplicant1PaymentOtherDetail", PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS)
            .field("paperFormApplicant2PaymentOtherDetail", PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS)
            .field("generalApplicationFeeOrderSummary")
            .field("generalApplicationFeePaymentMethod")
            .field("generalApplicationFeeAccountNumber")
            .field("generalApplicationFeeAccountReferenceNumber")
            .field("generalApplicationFeeHelpWithFeesReferenceNumber")
            .field(CaseData::getPaymentHistoryField);
    }

    private void buildDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("documents", "Documents")
            .field(CaseData::getGeneralEmails);
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
            .field("cicCaseApplicantAddress")
            .label("submission-details", null, "### Submission details")
            .field("dateSubmitted");
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
