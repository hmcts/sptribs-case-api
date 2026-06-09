import { expect, Page } from "@playwright/test";
import editSummarySelectHearingContent from "../../../fixtures/content/CaseAPI/editSummary/editSummarySelectHearing_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditSummarySelectHearingPage = {
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

const editSummarySelectHearingPage: EditSummarySelectHearingPage = {
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
      `.govuk-heading-l:text-is("${editSummarySelectHearingContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editSummarySelectHearingContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editSummarySelectHearingContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        editSummarySelectHearingContent.textOnPage,
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
    await page.locator("#cicCaseHearingSummaryList").selectOption({ index: 1 });
    return await page.textContent(
      "#cicCaseHearingSummaryList > option:nth-child(2)",
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        editSummarySelectHearingContent.errorBanner,
      ),
      expect(page.locator(".error-message")).toHaveText(
        editSummarySelectHearingContent.chooseSummaryError,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        editSummarySelectHearingContent.chooseSummaryError,
      ),
    ]);
  },
};

export default editSummarySelectHearingPage;
