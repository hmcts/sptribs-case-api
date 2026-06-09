import { expect, Page } from "@playwright/test";
import selectOrder_content from "../../../fixtures/content/CaseAPI/manageDueDate/selectOrder_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SelectOrderPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  selectDropdownOption(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const selectOrderPage: SelectOrderPage = {
  previous: ".button-secondary[disabled]",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page,
    caseNumber,
    accessibilityTest,
    subjectName,
  ): Promise<void> {
    await page.waitForSelector(
      `.form-label:text-is("${selectOrder_content.textOnPage1}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        `${selectOrder_content.pageHint}`,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p")).toContainText(
        selectOrder_content.caseReference + caseNumber,
      ),
      expect(page.locator("#cicCaseOrderDynamicList")).toBeVisible(),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      //await new AxeUtils(page).audit();
      //awaiting XUI bug fix
    }
  },
  async selectDropdownOption(page): Promise<void> {
    await page.selectOption("#cicCaseOrderDynamicList", { index: 1 });
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${selectOrder_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${selectOrder_content.errorBlank}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${selectOrder_content.errorBlank}")`,
        ),
        1,
      ),
    ]);
    await this.selectDropdownOption(page);
  },
};

export default selectOrderPage;
