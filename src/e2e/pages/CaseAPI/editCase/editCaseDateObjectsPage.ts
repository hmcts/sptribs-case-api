import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseDateObjectsContent from "../../../fixtures/content/CaseAPI/editCase/editCaseDateObjects_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseDateObjectsPage = {
  previous: string;
  continue: string;
  cancel: string;
  day: string;
  month: string;
  year: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkAndFillInFields(page: Page, initialState: initialState): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseDateObjectsPage: EditCaseDateObjectsPage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  day: "#cicCaseCaseReceivedDate-day",
  month: "#cicCaseCaseReceivedDate-month",
  year: "#cicCaseCaseReceivedDate-year",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseDateObjectsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseDateObjectsContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseDateObjectsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (editCaseDateObjectsContent as any)[
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

  async checkAndFillInFields(
    page: Page,
    initialState: initialState,
  ): Promise<void> {
    const currentDate = new Date();
    await page.fill(this.day, `${currentDate.getDate()}`);
    await page.fill(this.month, `${currentDate.getMonth() + 1}`);
    await page.fill(this.year, `${currentDate.getFullYear()}`);
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.locator(this.day).clear();
    await page.locator(this.month).clear();
    await page.locator(this.year).clear();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseDateObjectsContent.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        editCaseDateObjectsContent.dateError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        editCaseDateObjectsContent.dateError,
      ),
    ]);
    await page.fill(this.day, "90");
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseDateObjectsContent.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        editCaseDateObjectsContent.validDateError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        editCaseDateObjectsContent.inlineValidDateError,
      ),
    ]);
    await page.locator(this.day).clear();
  },
};

export default editCaseDateObjectsPage;
