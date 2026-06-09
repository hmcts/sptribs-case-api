import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/createEditStay/confirm_content.ts";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  closeAndReturn: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void>;
  closeAndReturnToCase(page: Page): Promise<void>;
};

const createCaseConfirmPage: ConfirmPage = {
  closeAndReturn: ".button",

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `markdown > h1:text-is("${confirm_content.subTitle1}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h1")).toHaveText(confirm_content.pageTitle),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      await expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
    ]);

    if (DSSSubmitted) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h2:has-text("${confirm_content.textOnPage2}")`,
        ),
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
    await page.waitForSelector(`h2:text-is("History")`);
    await page.waitForSelector(`.mat-tab-label-content:text-is("Tasks")`);
  },
};

export default createCaseConfirmPage;
