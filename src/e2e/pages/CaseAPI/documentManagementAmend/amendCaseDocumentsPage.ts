import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import amendCaseDocuments_content from "../../../fixtures/content/CaseAPI/documentManagementAmend/amendCaseDocuments_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type AmendCaseDocumentsPage = {
  continue: string;
  previous: string;
  cancel: string;
  dropdown: string;
  message: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const amendDocumentsPage: AmendCaseDocumentsPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary[disabled]",
  cancel: ".cancel",
  dropdown: "#cicCaseSelectedDocumentCategory",
  message: "#cicCaseSelectedDocumentEmailContent",
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${amendCaseDocuments_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        amendCaseDocuments_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        amendCaseDocuments_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${amendCaseDocuments_content.textOnPage2}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${amendCaseDocuments_content.textOnPage3}")`,
        ),
        1,
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
  async fillFields(page: Page): Promise<void> {
    await page.selectOption(this.dropdown, amendCaseDocuments_content.category);
    await page.fill(this.message, amendCaseDocuments_content.message);
  },
  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default amendDocumentsPage;
