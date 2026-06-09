import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/closeCase/confirm_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  closeAndReturn: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    DSSSubmitted: boolean,
  ): Promise<void>;
  closeAndReturnToCase(page: Page): Promise<void>;
};

const createCaseConfirmPage: ConfirmPage = {
  closeAndReturn: ".button",

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    DSSSubmitted: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h1")).toHaveText(confirm_content.pageTitle),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h1:text-is("${confirm_content.subTitle1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h2:has-text("${confirm_content.textOnPage2}")`,
        ),
        1,
      ),
    ]);

    if (DSSSubmitted) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h2:has-text("${confirm_content.textOnPage}")`),
        1,
      );
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h2:has-text("${confirm_content.textOnPage1}")`,
        ),
        1,
      );
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async closeAndReturnToCase(page: Page): Promise<void> {
    await page.locator(this.closeAndReturn).click();
  },
};

export default createCaseConfirmPage;
