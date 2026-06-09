import { AxeUtils } from "@hmcts/playwright-common";
import { Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/createDraft/confirm_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  closeAndReturn: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  closeAndReturnToCase(page: Page): Promise<void>;
};

const createCaseConfirmPage: ConfirmPage = {
  closeAndReturn: ".button",

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h1:text-is("${confirm_content.subTitle1}")`),
        1,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async closeAndReturnToCase(page: Page): Promise<void> {
    await page.locator(this.closeAndReturn).click();
    await page.waitForSelector(`h2:text-is("History")`);
    await page.waitForSelector(`.mat-tab-label-content:text-is("Tasks")`);
  },
};

export default createCaseConfirmPage;
