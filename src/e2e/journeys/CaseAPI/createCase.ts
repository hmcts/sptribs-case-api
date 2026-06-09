import { Page } from "@playwright/test";
import { UserRole } from "../../config.ts";
import {
  caseRegion,
  Category,
  ContactPreference,
  Scheme,
  SubCategory,
} from "../../helpers/commonHelpers.ts";
import caseAPILoginPage from "../../pages/CaseAPI/caseList/caseAPILoginPage.ts";
import casesPage from "../../pages/CaseAPI/caseList/casesPage.ts";
import caseFilterPage from "../../pages/CaseAPI/createCase/caseFilterPage.ts";
import caseCategorisationDetailsPage from "../../pages/CaseAPI/createCase/caseCategorisationDetailsPage.ts";
import cicaCaseDetailsPage from "../../pages/CaseAPI/createCase/cicaCaseDetailsPage.ts";
import caseCICADecisionDatePage from "../../pages/CaseAPI/createCase/caseCICADecisionDatePage.ts";
import caseDateObjectsPage from "../../pages/CaseAPI/createCase/caseDateObjectsPage.ts";
import caseObjectsSubjectsPage from "../../pages/CaseAPI/createCase/caseObjectsSubjectsPage.ts";
import caseSubjectDetailsObjectPage from "../../pages/CaseAPI/createCase/caseSubjectDetailsObjectPage.ts";
import caseApplicantDetailsObjectPage from "../../pages/CaseAPI/createCase/caseApplicantDetailsObjectPage.ts";
import caseRepresentativeDetailsObjectPage from "../../pages/CaseAPI/createCase/caseRepresentativeDetailsObjectPage.ts";
import caseObjectsContactsPage from "../../pages/CaseAPI/createCase/caseObjectsContactsPage.ts";
import caseDocumentsUploadObjectPage from "../../pages/CaseAPI/createCase/caseDocumentsUploadObjectPage.ts";
import caseFurtherDetailsObjectPage from "../../pages/CaseAPI/createCase/caseFurtherDetailsObjectPage.ts";
import submitPage from "../../pages/CaseAPI/createCase/submitPage.ts";
import createCaseConfirmPage from "../../pages/CaseAPI/createCase/confirmPage.ts";

type CreateCase = {
  createCase(
    page: Page,
    user: UserRole,
    accessibilityTest: boolean,
    category: Category,
    subCategory: SubCategory,
    representative: boolean,
    applicant: boolean,
    contactPreference: ContactPreference,
    subjectName: string,
    representativeQualified: boolean,
    multipleFiles: boolean,
    schemeSelection: Scheme,
    caseRegionSelection: caseRegion,
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
    needLogin: boolean,
    errorMessaging: boolean,
    decisionDate?: Date,
  ): Promise<string>;
};

const createCase: CreateCase = {
  async createCase(
    page: Page,
    user: UserRole,
    accessibilityTest: boolean,
    category: Category,
    subCategory: SubCategory,
    representative: boolean,
    applicant: boolean,
    contactPreference: ContactPreference,
    subjectName: string,
    representativeQualified: boolean,
    multipleFiles: boolean,
    schemeSelection: Scheme,
    caseRegionSelection: caseRegion,
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
    needLogin: boolean,
    errorMessaging: boolean,
    decisionDate?: Date,
  ): Promise<string> {
    let caseNumber: any;
    if (needLogin) {
      await caseAPILoginPage.SignInUser(page, user);
      await casesPage.checkPageLoads(page, accessibilityTest);
    }
    await casesPage.createCase(page);
    await caseFilterPage.checkPageLoads(page, accessibilityTest);
    await caseFilterPage.fillInFields(page);
    switch (errorMessaging) {
      default:
        await caseCategorisationDetailsPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseCategorisationDetailsPage.fillInFields(
          page,
          category,
          subCategory,
        );
        await cicaCaseDetailsPage.checkPageLoads(page, accessibilityTest);
        await cicaCaseDetailsPage.fillInFields(page);
        await caseCICADecisionDatePage.checkPageLoads(page, accessibilityTest);
        await caseCICADecisionDatePage.fillInFields(page, decisionDate);
        await caseDateObjectsPage.checkPageLoads(page, accessibilityTest);
        await caseDateObjectsPage.fillInFields(page);
        await caseObjectsSubjectsPage.checkPageLoads(page, accessibilityTest);
        await caseObjectsSubjectsPage.fillInFields(
          page,
          representative,
          applicant,
          subCategory,
        );
        await caseSubjectDetailsObjectPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseSubjectDetailsObjectPage.fillInFields(
          page,
          contactPreference,
          subjectName,
        );
        if (applicant) {
          await caseApplicantDetailsObjectPage.checkPageLoads(
            page,
            accessibilityTest,
          );
          await caseApplicantDetailsObjectPage.fillInFields(
            page,
            contactPreference,
          );
        }
        if (representative) {
          await caseRepresentativeDetailsObjectPage.checkPageLoads(
            page,
            accessibilityTest,
          );
          await caseRepresentativeDetailsObjectPage.fillInFields(
            page,
            contactPreference,
            representativeQualified,
          );
        }
        await caseObjectsContactsPage.checkPageLoads(page, accessibilityTest);
        await caseObjectsContactsPage.fillInFields(
          page,
          subCategory,
          representative,
          applicant,
        );
        await caseDocumentsUploadObjectPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseDocumentsUploadObjectPage.fillInFields(page, multipleFiles);
        await caseFurtherDetailsObjectPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseFurtherDetailsObjectPage.fillInFields(
          page,
          schemeSelection,
          caseRegionSelection,
          claimsLinked,
          compensationLinked,
          tribunalFormsInTime,
          applicantExplained,
        );
        await submitPage.checkPageLoads(
          page,
          accessibilityTest,
          contactPreference,
          applicant,
          representative,
          multipleFiles,
          tribunalFormsInTime,
        );
        await submitPage.checkValidInfo(
          page,
          contactPreference,
          applicant,
          representative,
          multipleFiles,
          category,
          subCategory,
          schemeSelection,
          caseRegionSelection,
          representativeQualified,
          claimsLinked,
          compensationLinked,
          tribunalFormsInTime,
          applicantExplained,
          subjectName,
        );
        break;
      case true:
        await caseCategorisationDetailsPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseCategorisationDetailsPage.triggerErrorMessages(page);
        await caseCategorisationDetailsPage.fillInFields(
          page,
          category,
          subCategory,
        );
        await cicaCaseDetailsPage.checkPageLoads(page, accessibilityTest);
        await cicaCaseDetailsPage.fillInFields(page);
        await caseCICADecisionDatePage.checkPageLoads(page, accessibilityTest);
        await caseCICADecisionDatePage.fillInFields(page, decisionDate);
        await caseDateObjectsPage.checkPageLoads(page, accessibilityTest);
        await caseDateObjectsPage.triggerErrorMessages(page);
        await caseDateObjectsPage.fillInFields(page);
        await caseObjectsSubjectsPage.checkPageLoads(page, accessibilityTest);
        await caseObjectsSubjectsPage.triggerErrorMessages(page);
        await caseObjectsSubjectsPage.fillInFields(
          page,
          representative,
          applicant,
          subCategory,
        );
        await caseSubjectDetailsObjectPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseSubjectDetailsObjectPage.triggerErrorMessages(page);
        await caseSubjectDetailsObjectPage.fillInFields(
          page,
          contactPreference,
          subjectName,
        );
        if (applicant) {
          await caseApplicantDetailsObjectPage.checkPageLoads(
            page,
            accessibilityTest,
          );
          await caseApplicantDetailsObjectPage.triggerErrorMessages(page);
          await caseApplicantDetailsObjectPage.fillInFields(
            page,
            contactPreference,
          );
        }
        if (representative) {
          await caseRepresentativeDetailsObjectPage.checkPageLoads(
            page,
            accessibilityTest,
          );
          await caseRepresentativeDetailsObjectPage.triggerErrorMessages(page);
          await caseRepresentativeDetailsObjectPage.fillInFields(
            page,
            contactPreference,
            representativeQualified,
          );
        }
        await caseObjectsContactsPage.checkPageLoads(page, accessibilityTest);
        await caseObjectsContactsPage.triggerErrorMessages(page);
        await caseObjectsContactsPage.fillInFields(
          page,
          subCategory,
          representative,
          applicant,
        );
        await caseDocumentsUploadObjectPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseDocumentsUploadObjectPage.triggerErrorMessages(page);
        await caseDocumentsUploadObjectPage.fillInFields(page, multipleFiles);
        await caseFurtherDetailsObjectPage.checkPageLoads(
          page,
          accessibilityTest,
        );
        await caseFurtherDetailsObjectPage.triggerErrorMessages(page);
        await caseFurtherDetailsObjectPage.fillInFields(
          page,
          schemeSelection,
          caseRegionSelection,
          claimsLinked,
          compensationLinked,
          tribunalFormsInTime,
          applicantExplained,
        );
        await submitPage.checkPageLoads(
          page,
          accessibilityTest,
          contactPreference,
          applicant,
          representative,
          multipleFiles,
          tribunalFormsInTime,
        );
        await submitPage.checkValidInfo(
          page,
          contactPreference,
          applicant,
          representative,
          multipleFiles,
          category,
          subCategory,
          schemeSelection,
          caseRegionSelection,
          representativeQualified,
          claimsLinked,
          compensationLinked,
          tribunalFormsInTime,
          applicantExplained,
          subjectName,
        );
        break;
    }
    await createCaseConfirmPage.checkPageLoads(page, accessibilityTest);
    caseNumber = await createCaseConfirmPage.returnCaseNumber(page);
    await createCaseConfirmPage.closeAndReturnToCase(page);
    return caseNumber;
  },
};

export default createCase;
