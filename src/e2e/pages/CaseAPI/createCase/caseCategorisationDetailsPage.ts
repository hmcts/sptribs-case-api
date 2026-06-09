import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseCategorisationDetails_content from "../../../fixtures/content/CaseAPI/createCase/caseCategorisationDetails_content.ts";
import commonHelpers, {
  Category,
  SubCategory,
} from "../../../helpers/commonHelpers.ts";

type CaseCategorisationDetailsPage = {
  continue: string;
  category: string;
  subCategory: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(
    page: Page,
    category: Category,
    subCategory: SubCategory,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const caseCategorisationDetailsPage: CaseCategorisationDetailsPage = {
  continue: '[type="submit"]',
  category: "#cicCaseCaseCategory",
  subCategory: "#cicCaseCaseSubcategory",

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${caseCategorisationDetails_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        caseCategorisationDetails_content.pageHint,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (caseCategorisationDetails_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(
    page: Page,
    category: string,
    subCategory: string,
  ): Promise<void> {
    await page.selectOption(this.category, category);
    await page.selectOption(this.subCategory, subCategory);
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        caseCategorisationDetails_content.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        caseCategorisationDetails_content.categoryError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        caseCategorisationDetails_content.subcategoryError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        caseCategorisationDetails_content.categoryError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        caseCategorisationDetails_content.subcategoryError,
      ),
    ]);
  },
};

export default caseCategorisationDetailsPage;
