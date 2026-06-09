import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import submit_content from "../../../fixtures/content/CaseAPI/createFlag/submit_content.ts";

type SubmitPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  verifyFlagCreated(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  previous: `.button-secondary:text-is("Previous")`,
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page,
    caseNumber,
    accessibilityTest,
    subjectName,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submit_content.pageTitle}")`,
    );

    await page.waitForSelector(`markdown > h3:text-is("${subjectName}")`);

    await page.waitForSelector(
      `markdown > p:text-is("${submit_content.caseReference + caseNumber}")`,
    );

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page): Promise<void> {
    await page.click(this.continue);
    await page.click(this.continue);
  },

  async verifyFlagCreated(page): Promise<void> {
    const banner = page.locator(".govuk-notification-banner");
    await expect(banner).toBeVisible();
  },
};

export default submitPage;
