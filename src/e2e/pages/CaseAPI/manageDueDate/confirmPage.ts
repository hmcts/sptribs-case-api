import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/manageDueDate/confirm-content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  closeAndReturn: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  closeAndReturnToCase(page: Page): Promise<void>;
};

const manageDueDateConfirmPage: ConfirmPage = {
  closeAndReturn: ".button",

  async checkPageLoads(
    page,
    accessibilityTest,
    caseNumber,
    subjectName,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h1:text-is("${confirm_content.subTitle1}")`),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        confirm_content.caseReference + caseNumber,
      ),
    ]);

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async closeAndReturnToCase(page: Page): Promise<void> {
    await page.locator(this.closeAndReturn).click();
    await page.waitForLoadState("load");
  },
};

export default manageDueDateConfirmPage;
