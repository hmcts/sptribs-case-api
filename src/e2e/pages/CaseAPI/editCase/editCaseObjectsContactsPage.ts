import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseObjectsContactsContent from "../../../fixtures/content/CaseAPI/editCase/editCaseObjectsContacts_content.ts";
import commonHelpers, { SubCategory } from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseObjectsContactsPage = {
  previous: string;
  continue: string;
  cancel: string;
  subjectSelectBox: string;
  representativeSelectBox: string;
  applicantSelectBox: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkAndFillInFields(
    page: Page,
    initialState: initialState,
    caseType: SubCategory,
    representative: boolean,
    applicant: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseObjectsContactsPage: EditCaseObjectsContactsPage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  subjectSelectBox: "#cicCaseSubjectCIC-SubjectCIC",
  applicantSelectBox: "#cicCaseApplicantCIC-ApplicantCIC",
  representativeSelectBox: "#cicCaseRepresentativeCIC-RepresentativeCIC",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseObjectsContactsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseObjectsContactsContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        editCaseObjectsContactsContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseObjectsContactsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 6 }, (_, index) => {
        const textOnPage = (editCaseObjectsContactsContent as any)[
          `textOnPage${index + 1}`
        ];
        return expect(page.locator(".form-label").nth(index)).toHaveText(
          textOnPage,
        );
      }),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkAndFillInFields(
    page: Page,
    initialState: initialState,
    subCategory: SubCategory,
    representative: boolean,
    applicant: boolean,
  ): Promise<void> {
    if (!(subCategory === "Fatal" || subCategory === "Minor")) {
      await expect(page.locator(this.subjectSelectBox)).toBeChecked();
    } else {
      await page.click(this.subjectSelectBox);
    }
    if (initialState !== "DSS Submitted") {
      if (representative) {
        await expect(page.locator(this.representativeSelectBox)).toBeChecked();
      }
      if (applicant) {
        await expect(page.locator(this.applicantSelectBox)).toBeChecked();
      }
    } else {
      if (representative) {
        await expect(page.locator(this.representativeSelectBox)).toBeChecked();
      }
      if (applicant) {
        await page.click(this.applicantSelectBox);
      }
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.subjectSelectBox);
    await page.click(this.representativeSelectBox);
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".error-summary-heading").nth(1)).toHaveText(
        editCaseObjectsContactsContent.errorBanner,
      ),
      expect(page.locator(".error-summary-list")).toContainText(
        editCaseObjectsContactsContent.errorMessage,
      ),
    ]);
    await page.click(this.subjectSelectBox);
    await page.click(this.representativeSelectBox);
  },
};

export default editCaseObjectsContactsPage;
