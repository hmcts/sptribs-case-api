import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editDraftAddDocumentFooterContent from "../../../fixtures/content/CaseAPI/editDraft/editDraftAddDocumentFooter_content.ts";
import editDraftOrderMainContentContent from "../../../fixtures/content/CaseAPI/editDraft/editDraftOrderMainContent_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/editDraft/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkAllInfo(page: Page, editedTemplate: string | null): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.text-16:text-is("${submit_content.textOnPage1}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.govuk-heading-l:text-is("${submit_content.pageHint}")`),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}")`),
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

  async checkAllInfo(page: Page, editedTemplate: string | null): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${editedTemplate}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span:text-is("${editDraftOrderMainContentContent.editDescription}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${editDraftAddDocumentFooterContent.editSignature}")`,
        ),
        1,
      ),
    ]);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
