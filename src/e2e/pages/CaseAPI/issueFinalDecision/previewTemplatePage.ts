import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import previewTemplate_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/previewTemplate_content.ts";
import commonHelpers, {
  CaseNoticeType,
} from "../../../helpers/commonHelpers.ts";
import { Template } from "./selectTemplatePage.ts";

type PreviewTemplatePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void>;
  fillInFields(
    page: Page,
    template: Template,
    caseNumber: string,
    caseNoticeType: CaseNoticeType,
  ): Promise<void>;
};

const previewTemplatePage: PreviewTemplatePage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${previewTemplate_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        previewTemplate_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(
        caseSubjectDetailsObject_content.name,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        previewTemplate_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.case-field__label:text-is("${previewTemplate_content.textOnPage1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:text-is("${previewTemplate_content.textOnPage2}")`),
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
  ): Promise<void> {
    await commonHelpers.checkDocument(
      page,
      template,
      caseNumber,
      caseNoticeType,
      false,
    );
    await page.click(this.continue);
  },
};

export default previewTemplatePage;
