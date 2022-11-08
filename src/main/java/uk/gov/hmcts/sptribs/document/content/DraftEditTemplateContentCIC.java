package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.content.provider.ApplicationTemplateDataProvider;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.JURISDICTIONS;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.GENERAL_DIRECTIONS;


@Component
@Slf4j
public class DraftEditTemplateContentCIC {


    @Autowired
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    public Map<String, Object> apply(final CaseData caseData, final Long caseId) {

        final Map<String, Object> templateContent = new HashMap<>();

        //log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());
        log.info("Sending document request for template : {} case id: {}",caseId,caseData.getDraftOrderCIC());
        final Application application = caseData.getApplication();
//        final Applicant applicant1 = caseData.getApplicant1();
//        final Applicant applicant2 = caseData.getApplicant2();

        if(caseData.getDraftOrderCIC().getOrderTemplate().equals(GENERAL_DIRECTIONS)){
            templateContent.put(GENERAL_DIRECTIONS,"for a fn");

        }

//        if (caseData.getDivorceOrDissolution().isDivorce()) {
//            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce.");
//            templateContent.put(DIVORCE_OR_DISSOLUTION, "divorce application");
//            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
//            templateContent.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
//            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce");
//        } else {
//            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for the dissolution of their civil partnership.");
//            templateContent.put(DIVORCE_OR_DISSOLUTION, "application to end a civil partnership");
//            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
//            templateContent.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
//            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership");
//        }

//        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
//        if (application.getIssueDate() != null) {
//            templateContent.put(ISSUE_DATE, application.getIssueDate().format(DATE_TIME_FORMATTER));
//        }
//        templateContent.put(ISSUE_DATE_POPULATED, application.getIssueDate() != null);
//
//        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
//        templateContent.put(APPLICANT_1_MIDDLE_NAME, applicant1.getMiddleName());
//        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
//        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
//        templateContent.put(APPLICANT_1_MARRIAGE_NAME, application.getMarriageDetails().getApplicant1Name());
//        templateContent.put(APPLICANT_1_POSTAL_ADDRESS, applicant1.getCorrespondenceAddress());
//        if (!applicant1.isConfidentialContactDetails()) {
//            templateContent.put(APPLICANT_1_EMAIL, applicant1.getEmail());
//        }
//        if (null != applicant1.getFinancialOrder()) {
//            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_1, applicant1.getFinancialOrder().toBoolean());
//            templateContent.put(APPLICANT_1_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveJointFinancialOrder(applicant1));
//        }
//        if (null != applicant1.getLegalProceedings()) {
//            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_1, applicant1.getLegalProceedings().toBoolean());
//            templateContent.put(APPLICANT_1_COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());
//        }
//
//        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
//        templateContent.put(APPLICANT_2_MIDDLE_NAME, applicant2.getMiddleName());
//        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
//        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
//        templateContent.put(APPLICANT_2_MARRIAGE_NAME, application.getMarriageDetails().getApplicant2Name());
//
//        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);
//
//        if (null != applicant2.getFinancialOrder()) {
//            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_2, applicant2.getFinancialOrder().toBoolean());
//            templateContent.put(APPLICANT_2_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveJointFinancialOrder(applicant2));
//        }
//        if (null != applicant2.getLegalProceedings()) {
//            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_2, applicant2.getLegalProceedings().toBoolean());
//            templateContent.put(APPLICANT_2_COURT_CASE_DETAILS, applicant2.getLegalProceedingsDetails());
//        }

        //applicationTemplateDataProvider.mapDratTemplateDetails(templateContent, application);

        templateContent.put(JURISDICTIONS, applicationTemplateDataProvider.deriveJurisdictionList(application, caseId));

        return templateContent;
    }
}
