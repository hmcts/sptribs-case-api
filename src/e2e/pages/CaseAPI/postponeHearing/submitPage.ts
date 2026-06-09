import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import postponeHearingNotifyPageContent from "../../../fixtures/content/CaseAPI/postponeHearing/postponeHearingNotifyPage_content.ts";
import postponeHearingReasonContent from "../../../fixtures/content/CaseAPI/postponeHearing/postponeHearingReason_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/postponeHearing/submit_content.ts";
import commonHelpers, {
  hearingPostponedReasons,
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
    reasonPostponed: hearingPostponedReasons,
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
    await page.waitForURL(
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/caseworker-postpone-hearing/submit`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        submitContent.pageTitle,
      ),
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
        3,
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
    reasonPostponed: hearingPostponedReasons,
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
          `ccd-read-fixed-radio-list-field > span.text-16:text-is("${reasonPostponed}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-area-field > span:text-is("${postponeHearingReasonContent.otherImportantInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span:text-is("${postponeHearingNotifyPageContent.textOnPage3}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${postponeHearingNotifyPageContent.textOnPage4}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${postponeHearingNotifyPageContent.textOnPage5}")`,
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
