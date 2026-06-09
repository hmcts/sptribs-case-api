import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import previewOrderPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/previewOrderPage_content.ts";
import commonHelpers, {
  CaseNoticeType,
} from "../../../helpers/commonHelpers.ts";
import { Template } from "../issueFinalDecision/selectTemplatePage.ts";

type PreviewOrderPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    template: Template,
    caseNumber: string,
    caseNoticeType: CaseNoticeType,
    subjectName: string,
  ): Promise<void>;
};

const previewOrderPage: PreviewOrderPage = {
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
      `.govuk-heading-l:text-is("${previewOrderPage_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        previewOrderPage_content.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        previewOrderPage_content.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        previewOrderPage_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.case-field__label:text-is("${previewOrderPage_content.textOnPage1}")`,
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

  async fillInFields(
    page: Page,
    template: Template,
    caseNumber: string,
    caseNoticeType: CaseNoticeType,
    subjectName: string,
  ): Promise<void> {
    await commonHelpers.checkDocument(
      page,
      template,
      caseNumber,
      caseNoticeType,
      false,
      subjectName,
    );
    await page.click(this.continue);
  },
};

export default previewOrderPage;
