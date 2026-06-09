import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCICACaseDetailsEditCaseDetailsContent from "../../../fixtures/content/CaseAPI/editCICACaseDetails/editCICACaseDetailsEditCaseDetails_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/editCICACaseDetails/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

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
  checkValidInfo(page: Page): Promise<void>;
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
      expect(page.locator("markdown > h3").first()).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submitContent.caseReference + caseNumber,
      ),
      expect(page.locator(".heading-h2")).toHaveText(submitContent.subTitle1),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
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

  async checkValidInfo(page: Page): Promise<void> {
    await Promise.all([
      expect(page.locator("ccd-read-text-field").nth(0)).toHaveText(
        editCICACaseDetailsEditCaseDetailsContent.referenceNumber,
      ),
      expect(page.locator("ccd-read-text-field").nth(1)).toHaveText(
        editCICACaseDetailsEditCaseDetailsContent.caseWorker,
      ),
      expect(page.locator("ccd-read-text-field").nth(2)).toHaveText(
        editCICACaseDetailsEditCaseDetailsContent.presentingOfficer,
      ),
    ]);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
