import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import selectOrderPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/selectOrderPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type OrderType = "DraftOrder" | "UploadOrder";

type SelectOrderIssuingTypePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page, orderType: OrderType): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const selectOrderIssuingTypePage: SelectOrderIssuingTypePage = {
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
      `.govuk-heading-l:text-is("${selectOrderPage_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${selectOrderPage_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        selectOrderPage_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 3 }, (_, index: number) => {
        const textOnPage: ArrayConstructor = (selectOrderPage_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
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

  async fillInFields(page: Page, orderType: OrderType): Promise<void> {
    await page.getByLabel("Create and send a new order").click();
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${selectOrderPage_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${selectOrderPage_content.errorBlank}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${selectOrderPage_content.errorBlank}")`,
        ),
        1,
      ),
    ]);
    await this.fillInFields(page, "DraftOrder");
  },
};

export default selectOrderIssuingTypePage;
