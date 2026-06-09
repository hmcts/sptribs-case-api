import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import referCaseToJudgeReasonContent from "../../../fixtures/content/CaseAPI/referCaseToJudge/referCaseToJudgeReason_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/referCaseToJudge/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { referralReason } from "./referCaseToJudgeReasonPage.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    referralReason: referralReason,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkAndFillInfo(page: Page, referralReason: referralReason): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    referralReason: referralReason,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submitContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submitContent.caseReference + caseNumber,
      ),
      expect(page.locator(".heading-h2")).toHaveText(submitContent.subTitle),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage2}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.form-label:text-is("${submitContent.textOnPage3}")`),
        1,
      ),
      expect(page.locator(".form-hint")).toHaveText(submitContent.textOnPage4),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.form-label:text-is("${submitContent.textOnPage5}")`),
        1,
      ),
      commonHelpers.checkForButtons(
        page,
        this.saveAndContinue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (referralReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage6}")`,
        ),
        1,
      );
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkAndFillInfo(
    page: Page,
    referralReason: referralReason,
  ): Promise<void> {
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `ccd-read-fixed-list-field > span.text-16:text-is("${referralReason}")`,
      ),
      1,
    );
    if (referralReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${referCaseToJudgeReasonContent.reason}")`,
        ),
        1,
      );
    }
    await page.fill("#field-trigger-summary", submitContent.summary);
    await page.fill("#field-trigger-description", submitContent.description);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
