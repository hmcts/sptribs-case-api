import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import flagCommentsPage_content from "../../../fixtures/content/CaseAPI/createFlag/flagCommentsPage_content.ts";

type FlagCommentsPage = {
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
};

const flagCommentsPage: FlagCommentsPage = {
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
      `.govuk-heading-l:text-is("${flagCommentsPage_content.pageTitle}")`,
    );

    await page.waitForSelector(
      `.govuk-caption-l:text-is("${flagCommentsPage_content.pageHint}")`,
    );

    await page.waitForSelector(`markdown > h3:text-is("${subjectName}")`);

    await page.waitForSelector(
      `markdown > p:text-is("${flagCommentsPage_content.caseReference + caseNumber}")`,
    );

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page): Promise<void> {
    const selector = `#flagComments`;
    await page.fill(selector, "Lorem Ipsum");
    await page.click(this.continue);
  },
};

export default flagCommentsPage;
