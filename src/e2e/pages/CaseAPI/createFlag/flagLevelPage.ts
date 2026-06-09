import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import flagLevelPage_content from "../../../fixtures/content/CaseAPI/createFlag/flagLevelPage_content.ts";

type FlagLevelPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page, flagLevel: number): Promise<void>;
};

const flagLevelPage: FlagLevelPage = {
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
      `.govuk-heading-l:text-is("${flagLevelPage_content.pageTitle}")`,
    );

    await page.waitForSelector(
      `.govuk-caption-l:text-is("${flagLevelPage_content.pageHint}")`,
    );

    await page.waitForSelector(`markdown > h3:text-is("${subjectName}")`);

    await page.waitForSelector(
      `markdown > p:text-is("${flagLevelPage_content.caseReference + caseNumber}")`,
    );

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page, flagLevel): Promise<void> {
    await page.locator(`#flag-location-${flagLevel}`).click();
    await page.click(this.continue);
  },
};

export default flagLevelPage;
