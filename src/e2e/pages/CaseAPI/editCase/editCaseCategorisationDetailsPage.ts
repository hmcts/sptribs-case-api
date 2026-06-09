import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseCategorisationDetailsContent from "../../../fixtures/content/CaseAPI/editCase/editCaseCategorisationDetails_content.ts";
import commonHelpers, {
  Category,
  SubCategory,
} from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseCategorisationDetailsPage = {
  previous: string;
  continue: string;
  cancel: string;
  category: string;
  subCategory: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkAndFillInFields(
    page: Page,
    initialState: initialState,
    category: Category,
    subCategory: SubCategory,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseCategorisationDetailsPage: EditCaseCategorisationDetailsPage = {
  previous: ".button-secondary[disabled]",
  continue: '[type="submit"]',
  cancel: ".cancel",
  category: "#cicCaseCaseCategory",
  subCategory: "#cicCaseCaseSubcategory",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseCategorisationDetailsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseCategorisationDetailsContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseCategorisationDetailsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (editCaseCategorisationDetailsContent as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
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
    category: string,
    subCategory: string,
  ): Promise<void> {
    if (initialState !== "DSS Submitted") {
      await Promise.all([
        expect(page.locator(this.category)).toHaveValue("1: Assessment"),
        expect(page.locator(this.subCategory)).toHaveValue("7: other"),
      ]);
    }
    await page.selectOption(this.category, category);
    await page.selectOption(this.subCategory, subCategory);
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseCategorisationDetailsContent.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        editCaseCategorisationDetailsContent.categoryError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        editCaseCategorisationDetailsContent.subcategoryError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        editCaseCategorisationDetailsContent.categoryError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        editCaseCategorisationDetailsContent.subcategoryError,
      ),
    ]);
  },
};

export default editCaseCategorisationDetailsPage;
