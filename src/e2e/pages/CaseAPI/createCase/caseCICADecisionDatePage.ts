import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseCICADecisionDate_content from "../../../fixtures/content/CaseAPI/createCase/caseCICADecisionDate_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CaseCICADecisionDatePage = {
  continue: string;
  day: string;
  month: string;
  year: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(page: Page, decisionDate?: Date): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const caseCICADecisionDatePage: CaseCICADecisionDatePage = {
  continue: '[type="submit"]',
  day: "#cicCaseInitialCicaDecisionDate-day",
  month: "#cicCaseInitialCicaDecisionDate-month",
  year: "#cicCaseInitialCicaDecisionDate-year",

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${caseCICADecisionDate_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        caseCICADecisionDate_content.pageHint,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (caseCICADecisionDate_content as any)[
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

  async fillInFields(page: Page, decisionDate?: Date): Promise<void> {
    const currentDate = decisionDate ?? new Date();
    const yesterday = new Date(currentDate);
    yesterday.setDate(currentDate.getDate() - 1);
    await page.fill(this.day, `${yesterday.getDate()}`);
    await page.fill(this.month, `${yesterday.getMonth() + 1}`);
    await page.fill(this.year, `${yesterday.getFullYear()}`);
    await page.click(this.continue);
    if (page.url().includes("DecisionDateObjects")) {
      await page.click(this.continue); // This is here in the chance that the "continue" button does not continue
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        caseCICADecisionDate_content.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        caseCICADecisionDate_content.dateError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        caseCICADecisionDate_content.dateError,
      ),
    ]);
    await page.fill(this.day, "90");
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        caseCICADecisionDate_content.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        caseCICADecisionDate_content.validDateError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        caseCICADecisionDate_content.inlineValidDateError,
      ),
    ]);
    await page.locator(this.day).clear();
  },
};

export default caseCICADecisionDatePage;
