import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import { UserRole } from "../../../config.ts";
import confirm_content from "../../../fixtures/content/CaseAPI/contactParties/confirm_content.ts";
import partiesToContact_content from "../../../fixtures/content/CaseAPI/contactParties/partiesToContact_content.ts";
import selectDocument_content from "../../../fixtures/content/CaseAPI/contactParties/selectDocument_content.ts";

type ConfirmPage = {
  continue: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    user: UserRole,
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
    user: UserRole,
    subjectName: string,
  ): Promise<void> {
    const headingRegex = new RegExp(
      `${confirm_content.textOnPage2}|${confirm_content.textOnPage3}`,
    );
    if (user === "respondent") {
      await page.waitForSelector(
        `.heading-h1:text-is("${selectDocument_content.pageHintRespondent}")`,
      );
    } else {
      await page.waitForSelector(
        `.heading-h1:text-is("${selectDocument_content.pageHint}")`,
      );
    }
    await Promise.all([
      expect(page.locator("markdown > h3").nth(0)).toHaveText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toHaveText(
        partiesToContact_content.caseReference + caseNumber,
      ),
      expect(page.locator("markdown > h1").nth(0)).toHaveText(
        confirm_content.textOnPage1,
      ),
      expect(page.locator("markdown > h2")).toHaveText(headingRegex),
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
