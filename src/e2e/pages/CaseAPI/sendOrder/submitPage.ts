import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import orderDueDates_content from "../../../fixtures/content/CaseAPI/sendOrder/orderDueDates_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/sendOrder/submit_content.ts";
import uploadOrder_Content from "../../../fixtures/content/CaseAPI/sendOrder/uploadOrder_Content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { OrderType } from "./selectOrderIssuingTypePage.ts";
import { ReminderDays } from "./sendReminderPage.ts";

type SubmitPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    orderType: OrderType,
    completed: boolean,
    reminder: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    orderType: OrderType,
    completed: boolean,
    reminder: boolean,
    reminderDays: ReminderDays,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    orderType: OrderType,
    completed: boolean,
    reminder: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h2:text-is("${submit_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        submit_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const textOnPage: ArrayConstructor = (submit_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}"):visible`),
          1,
        );
      }),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const dueDate: ArrayConstructor = (submit_content as any)[
          `dueDate${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${dueDate}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage3}")`),
        4,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (completed) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.dueDate4}")`),
        1,
      );
    }
    switch (orderType) {
      default: //draft order
        await commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${submit_content.draft1}")`),
          1,
        );
        break;
      case "UploadOrder":
        await Promise.all([
          ...Array.from({ length: 4 }, (_, index: number) => {
            const upload: ArrayConstructor = (submit_content as any)[
              `upload${index + 1}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(`.text-16:text-is("${upload}")`),
              1,
            );
          }),
        ]);
        break;
    }
    if (reminder) {
      await Promise.all([
        ...Array.from({ length: 2 }, (_, index: number) => {
          const reminder: ArrayConstructor = (submit_content as any)[
            `reminder${index + 1}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${reminder}")`),
            1,
          );
        }),
      ]);
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    orderType: OrderType,
    completed: boolean,
    reminder: boolean,
    reminderDays: ReminderDays,
  ): Promise<void> {
    switch (orderType) {
      default: //draft order
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${submit_content.draft}"):visible`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:has-text("draft.pdf")`),
            2,
          ),
        ]);
        break;
      case "UploadOrder":
        await page.waitForSelector(
          `ccd-read-document-field > button:text-is("${path.basename(config.testPdfFile)}")`,
        );
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${submit_content.upload}")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${uploadOrder_Content.description}")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-document-field > button:text-is("${path.basename(config.testPdfFile)}")`,
            ),
            1,
          ),
        ]);
        break;
    }
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("21 days")`),
        1,
      ),
    ]);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Subject")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Representative")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Respondent")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("Applicant (if different from subject)")`,
        ),
        1,
      ),
    ]);
    if (completed && reminder) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Yes")`),
        2,
      );
    }
    if (completed !== reminder) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Yes")`),
        1,
      );
    }
    if (reminder) {
      switch (reminderDays) {
        default:
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${reminderDays} ${submit_content.reminderDays}")`,
            ),
            1,
          );
          break;
        case "1":
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${reminderDays}${submit_content.reminderDay}")`,
            ),
            1,
          );
          break;
      }
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
