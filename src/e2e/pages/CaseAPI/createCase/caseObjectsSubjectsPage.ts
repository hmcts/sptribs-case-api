import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseObjectsSubjects_content from "../../../fixtures/content/CaseAPI/createCase/caseObjectsSubjects_content.ts";
import { SubCategory } from "../../../helpers/commonHelpers.ts";

type CaseObjectsSubjectsPage = {
  continue: string;
  subjectSelectBox: string;
  representativeSelectBox: string;
  applicantSelectBox: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(
    page: Page,
    representative: boolean,
    applicant: boolean,
    subcategory: SubCategory,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const caseObjectsSubjectsPage: CaseObjectsSubjectsPage = {
  continue: '[type="submit"]',
  subjectSelectBox: "#cicCasePartiesCIC-SubjectCIC",
  representativeSelectBox: "#cicCasePartiesCIC-RepresentativeCIC",
  applicantSelectBox: "#cicCasePartiesCIC-ApplicantCIC",

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${caseObjectsSubjects_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        caseObjectsSubjects_content.pageHint,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (caseObjectsSubjects_content as any)[
          `textOnPage${index + 1}`
        ];
        return expect(page.locator(".form-label").nth(index)).toHaveText(
          textOnPage,
        );
      }),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(
    page: Page,
    representative: boolean,
    applicant: boolean,
    subCategory: SubCategory,
  ): Promise<void> {
    if (
      (!applicant && subCategory === "Minor") ||
      (!applicant && subCategory === "Fatal")
    ) {
      throw new Error("Cannot have a Minor or Fatal case with no applicant.");
    }
    await page.click(this.subjectSelectBox);
    if (representative) {
      await page.click(this.representativeSelectBox);
    }
    if (applicant) {
      await page.click(this.applicantSelectBox);
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        caseObjectsSubjects_content.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        caseObjectsSubjects_content.partiesError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        caseObjectsSubjects_content.partiesError,
      ),
    ]);
    await page.click(this.representativeSelectBox);
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".error-summary-list")).toContainText(
        caseObjectsSubjects_content.subjectError,
      ),
    ]);
    await page.click(this.representativeSelectBox);
  },
};

export default caseObjectsSubjectsPage;
