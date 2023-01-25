package uk.gov.hmcts.sptribs.ciccase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

@Component
public class ApplicationTab implements CCDConfig<CaseData, State, UserRole> {

    private static final String NEVER_SHOW = "applicationType=\"NEVER_SHOW\"";
    private static final String JOINT_APPLICATION = "applicationType=\"jointApplication\"";
    private static final String SOLE_APPLICATION = "applicationType=\"soleApplication\"";
    private static final String NOT_NEW_PAPER_CASE = "newPaperCase!=\"Yes\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildSoleApplicationTab(configBuilder);
        buildJointApplicationTab(configBuilder);
    }

    private void buildSoleApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForSoleApplication = configBuilder.tab("applicationDetailsSole", "Application")
            .showCondition("applicationType=\"soleApplication\"");

        addDynamicContentHiddenFields(tabBuilderForSoleApplication);
        addHeaderFields(tabBuilderForSoleApplication);
        addMarriageAndCertificate(tabBuilderForSoleApplication);
        addLegalConnections(tabBuilderForSoleApplication);
        addOtherProceedings(tabBuilderForSoleApplication);
        addService(tabBuilderForSoleApplication);
        addOtherCourtCases(tabBuilderForSoleApplication);
        addApplicant1StatementOfTruth(tabBuilderForSoleApplication);
    }

    private void buildJointApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForJointApplication = configBuilder.tab("applicationDetailsJoint", "Application")
            .showCondition("applicationType=\"jointApplication\"");

        addDynamicContentHiddenFields(tabBuilderForJointApplication);
        addHeaderFields(tabBuilderForJointApplication);
        addOtherCourtCases(tabBuilderForJointApplication);
        addApplicant1StatementOfTruth(tabBuilderForJointApplication);
        addMarriageAndCertificate(tabBuilderForJointApplication);
        addLegalConnections(tabBuilderForJointApplication);
        addOtherProceedings(tabBuilderForJointApplication);
        addService(tabBuilderForJointApplication);
    }

    private void addDynamicContentHiddenFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("labelContentTheApplicantOrApplicant1", NEVER_SHOW)
            .field("labelContentTheApplicantOrApplicant1UC", NEVER_SHOW)
            .field("labelContentApplicantsOrApplicant1s", NEVER_SHOW)
            .field("labelContentTheApplicant2", NEVER_SHOW)
            .field("labelContentTheApplicant2UC", NEVER_SHOW)
            .field("labelContentApplicant2UC", NEVER_SHOW)
            .field("labelContentGotMarriedOrFormedCivilPartnership", NEVER_SHOW)
            .field("labelContentMarriageOrCivilPartnership", NEVER_SHOW)
            .field("labelContentMarriageOrCivilPartnershipUC", NEVER_SHOW);
    }

    private void addHeaderFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("createdDate")
            .field("dateSubmitted")
            .field("issueDate")
            .field("dueDate")
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution)
            .field(CaseData::getDivorceUnit)
            .field(CaseData::getHyphenatedCaseRef, NEVER_SHOW);
    }

    private void addOtherCourtCases(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1OtherProceedings-Heading", null, "#### ${labelContentTheApplicantOrApplicant1UC}'s other proceedings:")
            .field("applicant1LegalProceedings")
            .field("applicant1LegalProceedingsDetails",
                "applicant1LegalProceedings=\"Yes\"")
            .field("applicant1FinancialOrder")
            .field("applicant1FinancialOrdersFor",
                "applicant1FinancialOrder=\"Yes\"");
    }

    private void addMarriageAndCertificate(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelMarriage-Heading",
                "divorceOrDissolution = \"divorce\"", "### Marriage and certificate")
            .label("LabelCivilPartnership-Heading",
                "divorceOrDissolution = \"dissolution\"",
                "### Civil partnership and certificate")
            .field("marriageDate")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage",
                "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field("marriageCountryOfMarriage",
                "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field("marriageCertificateInEnglish")
            .field("marriageCertifiedTranslation", "marriageCertificateInEnglish=\"No\"");
    }

    private void addLegalConnections(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelJurisdiction-Heading", null, "### Jurisdiction")
            .field("jurisdictionConnections");
    }

    private void addOtherProceedings(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("solUrgentCase")
            .field("solUrgentCaseSupportingInformation", "solUrgentCase=\"Yes\"")
            .field("solStatementOfReconciliationCertify")
            .field("solStatementOfReconciliationDiscussed")
            .field("solSignStatementOfTruth")
            .field("solStatementOfReconciliationName")
            .field("solStatementOfReconciliationFirm")
            .field("statementOfReconciliationComments");
    }

    private void addService(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("Label-SolicitorService", "serviceMethod=\"solicitorService\"", "### Solicitor Service")
            .field("serviceMethod", SOLE_APPLICATION)
            .field("solServiceDateOfService", "serviceMethod=\"solicitorService\"")
            .field("solServiceDocumentsServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceOnWhomServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceHowServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceServiceDetails",
                "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
            .field("solServiceAddressServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceBeingThe", "serviceMethod=\"solicitorService\"")
            .field("solServiceLocationServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceSpecifyLocationServed",
                "serviceMethod=\"solicitorService\" AND solServiceLocationServed=\"otherSpecify\"")
            .field("solServiceServiceSotName", "serviceMethod=\"solicitorService\"")
            .field("solServiceTruthStatement", "serviceMethod=\"solicitorService\" AND solServiceHowServed=\"*\"")
            .field("solServiceServiceSotFirm", "serviceMethod=\"solicitorService\"");
    }

    private void addApplicant1StatementOfTruth(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("applicant1StatementOfTruth");
    }
}
