import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import buildCase_content from "../../../fixtures/content/CaseAPI/buildCase/buildCase_content.ts";

type BuildCasePage = {
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const builtCasePage: BuildCasePage = {
  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${buildCase_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        buildCase_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p")).toContainText(
        buildCase_content.caseReference + caseNumber,
      ),
    ]);

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.getByRole("button", { name: "Submit" }).click();
  },
};

export default builtCasePage;
