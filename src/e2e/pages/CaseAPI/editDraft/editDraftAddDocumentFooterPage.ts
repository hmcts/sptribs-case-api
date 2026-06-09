import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import addDocumentFooter_content from "../../../fixtures/content/CaseAPI/createDraft/addDocumentFooter_content.ts";
import editDraftAddDocumentFooterContent from "../../../fixtures/content/CaseAPI/editDraft/editDraftAddDocumentFooter_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditDraftAddDocumentFooterPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editDraftAddDocumentFooterPage: EditDraftAddDocumentFooterPage = {
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
      `.govuk-heading-l:text-is("${editDraftAddDocumentFooterContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editDraftAddDocumentFooterContent.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`div > markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editDraftAddDocumentFooterContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (editDraftAddDocumentFooterContent as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`p:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${editDraftAddDocumentFooterContent.textOnPage3}")`,
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

  async fillInFields(page: Page): Promise<void> {
    await expect(page.locator("#orderContentOrderSignature")).toHaveValue(
      addDocumentFooter_content.signature,
    );
    await page.fill(
      `#orderContentOrderSignature`,
      editDraftAddDocumentFooterContent.editSignature,
    );
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.locator("#orderContentOrderSignature").clear();
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${addDocumentFooter_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${addDocumentFooter_content.errorNoEntry}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${addDocumentFooter_content.errorNoEntry}")`,
        ),
        1,
      ),
    ]);
  },
};

export default editDraftAddDocumentFooterPage;
