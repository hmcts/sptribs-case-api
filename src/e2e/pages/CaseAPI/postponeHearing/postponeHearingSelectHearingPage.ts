import { expect, Page } from "@playwright/test";
import postponeHearingSelectHearingContent from "../../../fixtures/content/CaseAPI/postponeHearing/postponeHearingSelectHearing_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type PostponeHearingSelectHearingPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<string | null>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const postponeHearingSelectHearingPage: PostponeHearingSelectHearingPage = {
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
      `.govuk-heading-l:text-is("${postponeHearingSelectHearingContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        postponeHearingSelectHearingContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        postponeHearingSelectHearingContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        postponeHearingSelectHearingContent.textOnPage,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    // if (accessibilityTest) {
    //   await new AxeUtils(page).audit();
    // }
  },

  async fillInFields(page: Page): Promise<string | null> {
    await page.locator("#cicCaseHearingList").selectOption({ index: 1 });
    return await page.textContent("#cicCaseHearingList > option:nth-child(2)");
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        postponeHearingSelectHearingContent.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        postponeHearingSelectHearingContent.chooseHearingError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        postponeHearingSelectHearingContent.chooseHearingError,
      ),
    ]);
  },
};

export default postponeHearingSelectHearingPage;
