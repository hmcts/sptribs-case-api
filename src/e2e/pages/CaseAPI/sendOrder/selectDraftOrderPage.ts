import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import selectDraftOrder_content from "../../../fixtures/content/CaseAPI/sendOrder/selectDraftOrder_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SelectDraftOrderPage = {
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
  triggerErrorMessages(page: Page): Promise<void>;
};

const selectDraftOrderPage: SelectDraftOrderPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.form-label:text-is("${selectDraftOrder_content.textOnPage1}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${selectDraftOrder_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        selectDraftOrder_content.caseReference + caseNumber,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.selectOption(`#cicCaseDraftOrderDynamicList`, { index: 1 });
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${selectDraftOrder_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${selectDraftOrder_content.errorBlank}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${selectDraftOrder_content.errorBlank}")`,
        ),
        1,
      ),
    ]);
    await page.click(this.previous);
  },
};

export default selectDraftOrderPage;
