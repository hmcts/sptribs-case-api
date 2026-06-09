import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import cancelHearingNotifyPageContent from "../../../fixtures/content/CaseAPI/cancelHearing/cancelHearingNotifyPage_content.ts";
import cancelHearingReason_content from "../../../fixtures/content/CaseAPI/cancelHearing/cancelHearingReason_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/cancelHearing/submit_content.ts";
import commonHelpers, {
  hearingCancelledReasons,
} from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    hearing: string | null,
    reasonCancelled: hearingCancelledReasons,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submitContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submitContent.caseReference + caseNumber,
      ),
      expect(page.locator(".heading-h2")).toHaveText(submitContent.subTitle),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage5}")`,
        ),
        4,
      ),
      commonHelpers.checkForButtons(
        page,
        this.saveAndContinue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    hearing: string | null,
    reasonCancelled: hearingCancelledReasons,
  ): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-dynamic-list-field > span.text-16:text-is("${hearing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-radio-list-field > span.text-16:text-is("${reasonCancelled}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-area-field > span:text-is("${cancelHearingReason_content.otherImportantInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span:text-is("${cancelHearingNotifyPageContent.textOnPage3}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${cancelHearingNotifyPageContent.textOnPage4}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${cancelHearingNotifyPageContent.textOnPage5}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${cancelHearingNotifyPageContent.textOnPage6}")`,
        ),
        1,
      ),
    ]);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
