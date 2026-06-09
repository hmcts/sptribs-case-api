import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import sendReminder_content from "../../../fixtures/content/CaseAPI/sendOrder/sendReminder_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type ReminderDays = "1" | "3" | "5" | "7" | void;

type SendReminderPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    reminder: boolean,
    days: ReminderDays,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const sendReminderPage: SendReminderPage = {
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
      `.form-label:text-is("${sendReminder_content.textOnPage1}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${sendReminder_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        sendReminder_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 3 }, (_, index: number) => {
        const textOnPage: ArrayConstructor = (sendReminder_content as any)[
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

  async fillInFields(
    page: Page,
    reminder: boolean,
    days: ReminderDays,
  ): Promise<void> {
    switch (reminder) {
      default: // True
        await page.click(`#cicCaseOrderReminderYesOrNo-Yes`);
        await Promise.all([
          ...Array.from({ length: 4 }, (_, index: number) => {
            const textOnPage: ArrayConstructor = (sendReminder_content as any)[
              `textOnPage${index + 4}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(`.form-label:text-is("${textOnPage}")`),
              1,
            );
          }),
        ]);
        await page.click(`[id^="cicCaseOrderReminderDays-${days}"]`);
        break;
      case false:
        await page.click(`#cicCaseOrderReminderYesOrNo-No`);
        break;
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${sendReminder_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${sendReminder_content.errorNoInput}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${sendReminder_content.errorNoInput}")`,
        ),
        1,
      ),
    ]);
    await page.click(`#cicCaseOrderReminderYesOrNo-Yes`);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${sendReminder_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${sendReminder_content.errorNoDay}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${sendReminder_content.errorNoDay}")`,
        ),
        1,
      ),
    ]);
  },
};

export default sendReminderPage;
