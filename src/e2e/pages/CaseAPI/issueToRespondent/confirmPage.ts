import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/issueToRespondent/confirm_content.ts";
import notifyOtherParties_content from "../../../fixtures/content/CaseAPI/issueToRespondent/notifyOtherParties_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/issueToRespondent/submit_content.ts";
import commonHelpers, { parties } from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    recipients: parties[],
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const confirmPage: ConfirmPage = {
  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    recipients: parties[],
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${submit_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        notifyOtherParties_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h2:text-is("${confirm_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h2:has-text("${confirm_content.textOnPage2}")`,
        ),
        1,
      ),
      ...Array.from({ length: recipients.length }, (_, index) => {
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`markdown > h2:has-text("${recipients[index]}")`),
          1,
        );
      }),
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

export default confirmPage;
