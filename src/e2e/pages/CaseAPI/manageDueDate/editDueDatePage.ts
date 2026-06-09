import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Locator, Page } from "@playwright/test";
import editDueDate_content from "../../../fixtures/content/CaseAPI/manageDueDate/editDueDate_content.ts";
import orderDueDates_content from "../../../fixtures/content/CaseAPI/sendOrder/orderDueDates_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditDueDatePage = {
  previous: string;
  continue: string;
  cancel: string;
  addNew: string;
  remove: string;
  dayField: string;
  monthField: string;
  yearField: string;
  informationField: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    completed: boolean,
    completedCheckboxChecked: boolean,
  ): Promise<void>;
  checkFields(page: Page, completed: boolean): Promise<void>;
  triggerErrorMessages(
    page: Page,
    completed: boolean,
    completedCheckboxChecked: boolean,
  ): Promise<void>;
};

const editDueDatePage: EditDueDatePage = {
  previous: `.button-secondary:text-is("Previous")`,
  continue: '[type="submit"]',
  cancel: ".cancel",
  addNew: ".write-collection-add-item__top",
  remove: `.button:text-is("Remove")`,
  dayField: "#dueDate-day",
  monthField: "#dueDate-month",
  yearField: "#dueDate-year",
  informationField: "#cicCaseOrderDueDates_0_information",

  async checkPageLoads(
    page,
    caseNumber,
    accessibilityTest,
    subjectName,
  ): Promise<void> {
    await page.waitForSelector(
      `h2:text-is("${editDueDate_content.subTitle1}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        `${editDueDate_content.pageHint}`,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").first()).toContainText(
        editDueDate_content.caseReference + caseNumber,
      ),
      expect(page.locator(this.addNew)).toBeVisible(),

      ...Array.from({ length: 7 }, (_, index: number) => {
        const textOnPage: ArrayConstructor = (editDueDate_content as any)[
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
      expect(page.locator(this.addNew)).toBeVisible(),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkFields(page: Page, completed: boolean): Promise<void> {
    const formatValue = (value: string) =>
      value.length === 1 ? `0${value}` : value;
    if (completed) {
      await expect(page.getByRole("checkbox")).toBeChecked();
    } else {
      await expect(page.getByRole("checkbox")).not.toBeChecked();
    }
    await page
      .getByRole("button", { name: "Add new" })
      .first()
      .click({ force: true });
    await Promise.all([
      ...Array.from({ length: 5 }, (_, index: number) => {
        const textOnPage: ArrayConstructor = (editDueDate_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          2,
        );
      }),
    ]);
    await page.locator("button:has-text('Remove')").first().click();
    await page.locator("button[title='Remove']").click();
  },

  async fillInFields(page, completed, completedCheckboxChecked): Promise<void> {
    await page.evaluate(() => {
      const input = document.querySelector(
        '[id="orderDueDates_1_dueDateOptions-28 days"]',
      ) as HTMLInputElement;
      input.click();
    });

    if (completed) {
      if (completedCheckboxChecked) {
        await page.getByRole("checkbox").check();
      } else {
        await page.getByRole("checkbox").uncheck();
      }
    } else {
      if (!completed) {
        if (completedCheckboxChecked) {
          await page.getByRole("checkbox").check();
        } else {
          await page.getByRole("checkbox").uncheck();
        }
      }
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(
    page: Page,
    completed: boolean,
    completedCheckboxChecked: boolean,
  ): Promise<void> {
    const dateFieldError: Locator = page.locator(
      `.error-message:has-text("${editDueDate_content.errorBlank2}")`,
    );

    expect(dateFieldError).not.toBeVisible();

    await page.locator(this.dayField).fill("");
    expect(dateFieldError).toBeVisible();

    await page.locator(this.dayField).fill(editDueDate_content.day);
    await page.locator(this.monthField).fill("");
    expect(dateFieldError).toBeVisible();

    await page.locator(this.monthField).fill(editDueDate_content.month);
    await page.locator(this.yearField).fill("");
    expect(dateFieldError).toBeVisible();

    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${editDueDate_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${editDueDate_content.errorBlank1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${editDueDate_content.errorBlank2}")`,
        ),
        1,
      ),
    ]);
    await this.fillInFields(page, completed, completedCheckboxChecked);
  },
};

export default editDueDatePage;
