import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import orderDueDatePage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/orderDueDatePage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type OrderDueDatePage = {
  previous: string;
  continue: string;
  cancel: string;
  addNew: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page, completed: boolean): Promise<void>;
};

const orderDueDatePage: OrderDueDatePage = {
  previous: `.button-secondary:text-is("Previous")`,
  continue: '[type="submit"]',
  cancel: ".cancel",
  addNew: ".write-collection-add-item__top",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `h2:text-is("${orderDueDatePage_content.subTitle1}")`,
    );
    await page.click(this.addNew);
    await expect(page.locator("#orderDueDates_0_dueDateOptions")).toBeVisible();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${orderDueDatePage_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        orderDueDatePage_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:text-is("${orderDueDatePage_content.subTitle2}")`),
        1,
      ),
      ...Array.from({ length: 7 }, (_, index: number) => {
        const textOnPage: ArrayConstructor = (orderDueDatePage_content as any)[
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

  async fillInFields(page: Page, completed: boolean): Promise<void> {
    await page.evaluate(() => {
      const input = document.querySelector(
        '[id="orderDueDates_0_dueDateOptions-21 days"]',
      ) as HTMLInputElement;
      input.click();
    });
    if (completed) {
      await page.click(
        `[id^="orderDueDates_0_orderMarkAsCompleted-Mark as completed"]`,
      );
    }
    await page.click(this.continue);
  },
};

export default orderDueDatePage;
