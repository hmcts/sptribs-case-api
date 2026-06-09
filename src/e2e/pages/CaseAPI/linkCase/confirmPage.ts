import { AxeUtils } from "@hmcts/playwright-common";
import { Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/LinkCase/confirm_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateCaseLinkCreateCaseLink3Page = {
  closeAndReturn: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(page: Page): Promise<void>;
};

const createCaseLinkCreateCaseLink3: CreateCaseLinkCreateCaseLink3Page = {
  closeAndReturn: '[type="submit"]',

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      page.locator(this.closeAndReturn).isVisible(),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h1:text-is("${confirm_content.textOnPage1}")`),
        1,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.click(this.closeAndReturn);
  },
};

export default createCaseLinkCreateCaseLink3;
