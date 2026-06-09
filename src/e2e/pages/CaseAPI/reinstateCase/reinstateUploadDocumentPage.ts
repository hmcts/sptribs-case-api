import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import config from "../../../config.ts";
import uploadDocuments_content from "../../../fixtures/content/CaseAPI/closeCase/uploadDocuments_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import reinstateUploadDocument_content from "../../../fixtures/content/CaseAPI/reinstateCase/reinstateUploadDocument_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ReinstateUploadDocumentPage = {
  continue: string;
  previous: string;
  cancel: string;
  addNew: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const reinstateUploadDocumentPage: ReinstateUploadDocumentPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary:text-is('Previous')",
  cancel: ".cancel",
  addNew: ".write-collection-add-item__top",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${reinstateUploadDocument_content.pageTitle}")`,
    );
    await page.click(this.addNew);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${reinstateUploadDocument_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (reinstateUploadDocument_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`markdown > p:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (reinstateUploadDocument_content as any)[
          `textOnPage${index + 3}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`ul > li:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (reinstateUploadDocument_content as any)[
          `textOnPage${index + 6}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`markdown > p:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (reinstateUploadDocument_content as any)[
          `textOnPage${index + 8}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
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

  async continueOn(page: Page): Promise<void> {
    await page.selectOption(
      `.ccd-dropdown`,
      `${reinstateUploadDocument_content.uploadedDocumentCategory}`,
    );
    await page.fill(
      `#cicCaseReinstateDocumentsUpload_0_documentEmailContent`,
      `${reinstateUploadDocument_content.uploadedDocumentDescription}`,
    );
    await page
      .locator(`#cicCaseReinstateDocumentsUpload_0_documentLink`)
      .setInputFiles(config.testPdfFile);
    await page.locator(".error-message").waitFor({ state: "hidden" });
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve, 5000)); // avoid ExUI rate limiting.
    await page
      .locator(`#cicCaseReinstateDocumentsUpload_0_documentLink`)
      .setInputFiles(config.testOdtFile);
    await expect(page.locator(".error-message")).toHaveText(
      uploadDocuments_content.errorMessage,
    );
    await new Promise((resolve) => setTimeout(resolve, 5000)); // avoid ExUI rate limiting.
    await this.continueOn(page);
  },
};

export default reinstateUploadDocumentPage;
