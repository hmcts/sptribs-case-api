import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import flagTypePage_content from "../../../fixtures/content/CaseAPI/createFlag/flagTypePage_content.ts";

type FlagTypePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page, flagType: number): Promise<void>;
};

const flagTypePage: FlagTypePage = {
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
      `.govuk-heading-l:text-is("${flagTypePage_content.pageTitle}")`,
    );

    await page.waitForSelector(
      `.govuk-caption-l:text-is("${flagTypePage_content.pageHint}")`,
    );

    await page.waitForSelector(`markdown > h3:text-is("${subjectName}")`);

    await page.waitForSelector(
      `markdown > p:text-is("${flagTypePage_content.caseReference + caseNumber}")`,
    );

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page, flagType): Promise<void> {
    await page.locator(`#flag-type-${flagType}`).click();
    await page.click(this.continue);
  },
};

export default flagTypePage;
