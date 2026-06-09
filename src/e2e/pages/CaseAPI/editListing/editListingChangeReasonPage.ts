import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editListingChangeReasonContent from "../../../fixtures/content/CaseAPI/editListing/editListingChangeReason_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditListingChangeReasonPage = {
  reason: string;
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editListingChangeReasonPage: EditListingChangeReasonPage = {
  reason: "#recordListingChangeReason",
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
      `.govuk-heading-l:text-is("${editListingChangeReasonContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editListingChangeReasonContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editListingChangeReasonContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        editListingChangeReasonContent.textOnPage,
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
    await page.fill(this.reason, editListingChangeReasonContent.reason);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        editListingChangeReasonContent.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        editListingChangeReasonContent.reasonError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        editListingChangeReasonContent.reasonError,
      ),
    ]);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default editListingChangeReasonPage;
