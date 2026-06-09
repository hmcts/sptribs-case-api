import { expect, Page } from "@playwright/test";
import createSummarySelectHearingContent from "../../../fixtures/content/CaseAPI/createSummary/createSummarySelectHearing_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateSummarySelectHearingPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<string | null>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createSummarySelectHearingPage: CreateSummarySelectHearingPage = {
  previous: ".button-secondary[disabled]",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${createSummarySelectHearingContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        createSummarySelectHearingContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createSummarySelectHearingContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        createSummarySelectHearingContent.textOnPage,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    // if (accessibilityTest) {
    //   await new AxeUtils(page).audit();
    // }
  },

  async fillInFields(page: Page): Promise<string | null> {
    await page.locator("#cicCaseHearingList").selectOption({ index: 1 });
    return await page.textContent("#cicCaseHearingList > option:nth-child(2)");
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        createSummarySelectHearingContent.errorBanner,
      ),
      expect(page.locator(".error-message")).toHaveText(
        createSummarySelectHearingContent.chooseHearingError,
      ),
    ]);
  },
};

export default createSummarySelectHearingPage;
