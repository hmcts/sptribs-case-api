import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editDraftPreviewTemplateContent from "../../../fixtures/content/CaseAPI/editDraft/editDraftPreviewTemplate_content.ts";
import commonHelpers, {
  CaseNoticeType,
} from "../../../helpers/commonHelpers.ts";
import { Template } from "../issueFinalDecision/selectTemplatePage.ts";

type EditDraftPreviewTemplatePage = {
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

const editDraftPreviewTemplatePage: EditDraftPreviewTemplatePage = {
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
      `.govuk-heading-l:text-is("${editDraftPreviewTemplateContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editDraftPreviewTemplateContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editDraftPreviewTemplateContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.case-field__label:text-is("${editDraftPreviewTemplateContent.textOnPage1}")`,
        ),
        1,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (editDraftPreviewTemplateContent as any)[
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
      true,
      subjectName,
    );
    await page.click(this.continue);
  },
};

export default editDraftPreviewTemplatePage;
