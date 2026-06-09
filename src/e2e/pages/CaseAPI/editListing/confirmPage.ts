import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import confirmContent from "../../../fixtures/content/CaseAPI/editListing/confirm_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const confirmPage: ConfirmPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `markdown > h1:text-is("${confirmContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h1")).toHaveText(confirmContent.pageHint),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        confirmContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const textOnPage = (confirmContent as any)[`textOnPage${index + 1}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `markdown > h2:nth-child(${index + 2}):text-is("${textOnPage}")`,
          ),
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
