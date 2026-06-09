import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import buildCase_content from "../../../fixtures/content/CaseAPI/buildCase/buildCase_content.ts";
import confirm_content from "../../../fixtures/content/CaseAPI/buildCase/confirm_content.ts";

type ConfirmPage = {
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const buildCaseConfirmPage: ConfirmPage = {
  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h1")).toContainText(
        confirm_content.subTitle1,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p")).toContainText(
        buildCase_content.caseReference + caseNumber,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page
      .getByRole("button", { name: "Close and Return to case details" })
      .click();
    await page.waitForSelector(`h2:text-is("History")`);
    await page.waitForSelector(`.mat-tab-label-content:text-is("Tasks")`);
  },
};

export default buildCaseConfirmPage;
