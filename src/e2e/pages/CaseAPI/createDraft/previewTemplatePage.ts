import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import previewTemplate_content from "../../../fixtures/content/CaseAPI/createDraft/previewTemplate_content.ts";
import commonHelpers, {
  CaseNoticeType,
} from "../../../helpers/commonHelpers.ts";
import { Template } from "../issueFinalDecision/selectTemplatePage.ts";

type PreviewTemplatePage = {
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

const previewTemplatePage: PreviewTemplatePage = {
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
      `.govuk-heading-l:text-is("${previewTemplate_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        previewTemplate_content.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        previewTemplate_content.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        previewTemplate_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.case-field__label:text-is("${previewTemplate_content.textOnPage1}")`,
        ),
        1,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (previewTemplate_content as any)[
          `textOnPage${index + 2}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`p:text-is("${textOnPage}")`),
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

export default previewTemplatePage;
