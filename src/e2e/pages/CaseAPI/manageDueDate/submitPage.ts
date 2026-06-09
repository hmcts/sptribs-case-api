import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editDueDate_content from "../../../fixtures/content/CaseAPI/manageDueDate/editDueDate_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/manageDueDate/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import editDueDatePage from "./editDueDatePage.ts";

type SubmitPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    completed: boolean,
    completedCheckboxChecked: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(page: Page, completedCheckboxChecked: boolean): Promise<void>;
  saveAndContinue(page: Page): Promise<void>;
  checkChangeLink(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
};

const submitPage: SubmitPage = {
  previous: `.button-secondary:text-is("Previous")`,
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page,
    caseNumber,
    accessibilityTest,
    completed,
    completedCheckboxChecked,
    subjectName,
  ): Promise<void> {
    await page.waitForSelector(`h2:text-is("${submit_content.pageTitle}")`);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.govuk-heading-l:text-is("${submit_content.pageHint}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span:text-is("${submit_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span:text-is("${submit_content.draft}")`),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const dueDate: ArrayConstructor = (submit_content as any)[
          `dueDate${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${dueDate}")`),
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

    if (completed) {
      if (completedCheckboxChecked) {
        await expect(
          page.locator(`.text-16:text-is("${submit_content.dueDate4}")`),
        ).toBeVisible();
      } else {
        await expect(
          page.locator(`.text-16:text-is("${submit_content.dueDate4}")`),
        ).not.toBeVisible();
      }
    } else {
      if (!completed) {
        if (completedCheckboxChecked) {
          await expect(
            page.locator(`.text-16:text-is("${submit_content.dueDate4}")`),
          ).toBeVisible();
        } else {
          await expect(
            page.locator(`.text-16:text-is("${submit_content.dueDate4}")`),
          ).not.toBeVisible();
        }
      }
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    completedCheckboxChecked: boolean,
  ): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("28 days")`),
        1,
      ),
    ]);
    if (completedCheckboxChecked) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Yes")`),
        1,
      );
    }
  },

  async checkChangeLink(
    page,
    caseNumber,
    accessibilityTest,
    subjectName,
  ): Promise<void> {
    await page.locator(`[aria-label="Change Due Date"]`).click();
    await page.waitForURL(
      /.*\/caseworker-amend-due-datecaseworkerAmendDueDateEditDueDate$/,
      { timeout: 30_000 },
    );
    await editDueDatePage.checkPageLoads(
      page,
      caseNumber,
      accessibilityTest,
      subjectName,
    );
    await page.click(this.continue);
    await page.waitForURL(/.*\/submit$/, { timeout: 30_000 });
  },

  async saveAndContinue(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
