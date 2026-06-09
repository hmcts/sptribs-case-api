import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/documentManagementAmend/confirm_content.ts";

type ConfirmPage = {
  continue: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const confirmPage: ConfirmPage = {
  continue: '[type="submit"]',
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        confirm_content.caseReference + caseNumber,
      ),
      expect(page.locator("markdown > h1")).toContainText(
        confirm_content.textOnPage1,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },
  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default confirmPage;
