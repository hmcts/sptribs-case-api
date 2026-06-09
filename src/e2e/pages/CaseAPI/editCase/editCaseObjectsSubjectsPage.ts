import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseObjectsSubjectsContent from "../../../fixtures/content/CaseAPI/editCase/editCaseObjectsSubjects_content.ts";
import commonHelpers, { SubCategory } from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseObjectsSubjectsPage = {
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
    representative: boolean,
    applicant: boolean,
    subcategory: SubCategory,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseObjectsSubjectsPage: EditCaseObjectsSubjectsPage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  subjectSelectBox: "#cicCasePartiesCIC-SubjectCIC",
  representativeSelectBox: "#cicCasePartiesCIC-RepresentativeCIC",
  applicantSelectBox: "#cicCasePartiesCIC-ApplicantCIC",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseObjectsSubjectsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseObjectsSubjectsContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        editCaseObjectsSubjectsContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseObjectsSubjectsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (editCaseObjectsSubjectsContent as any)[
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
    await expect(page.locator(this.subjectSelectBox)).toBeChecked();
    if (initialState !== "DSS Submitted") {
      await Promise.all([
        expect(page.locator(this.representativeSelectBox)).toBeChecked(),
        expect(page.locator(this.applicantSelectBox)).toBeChecked(),
      ]);
      if (!representative) {
        await page.click(this.representativeSelectBox);
      }
      if (!applicant) {
        await page.click(this.applicantSelectBox);
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
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseObjectsSubjectsContent.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        editCaseObjectsSubjectsContent.partiesError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        editCaseObjectsSubjectsContent.partiesError,
      ),
    ]);
    await page.click(this.representativeSelectBox);
    await page.click(this.continue);
    await expect(page.locator(".error-summary-list")).toContainText(
      editCaseObjectsSubjectsContent.subjectError,
    );
    await page.click(this.subjectSelectBox);
  },
};

export default editCaseObjectsSubjectsPage;
