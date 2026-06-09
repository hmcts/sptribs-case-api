import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import config from "../../../config.ts";
import uploadDocuments_content from "../../../fixtures/content/CaseAPI/closeCase/uploadDocuments_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type UploadDocumentsPage = {
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

const uploadDocumentsPage: UploadDocumentsPage = {
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
      `.govuk-heading-l:text-is("${uploadDocuments_content.pageTitle}")`,
    );
    await page.click(this.addNew);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${uploadDocuments_content.pageHint}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:has-text("${uploadDocuments_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown:has-text("${uploadDocuments_content.textOnPage2}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:has-text("${uploadDocuments_content.textOnPage3}")`),
        1,
      ),

      ...Array.from({ length: 3 }, (_, index: number) => {
        const textOnPage = (uploadDocuments_content as any)[
          `textOnPage${index + 4}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:has-text("${uploadDocuments_content.subTitle1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h2:has-text("${uploadDocuments_content.subTitle2}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:has-text("${uploadDocuments_content.subTitle3}")`),
        1,
      ),
      await commonHelpers.checkForButtons(
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
      `${uploadDocuments_content.uploadedDocumentCategory}`,
    );
    await page.fill(
      `#closeDocumentsUpload_0_documentEmailContent`,
      `${uploadDocuments_content.uploadedDocumentDescription}`,
    );
    await page
      .locator(`#closeDocumentsUpload_0_documentLink`)
      .setInputFiles(config.testPdfFile);
    await page.locator(".error-message").waitFor({ state: "hidden" });
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve, 5000)); // avoid ExUI rate limiting.
    await page
      .locator(`#closeDocumentsUpload_0_documentLink`)
      .setInputFiles(config.testOdtFile);
    await expect(page.locator(".error-message")).toHaveText(
      uploadDocuments_content.errorMessage,
    );
    await new Promise((resolve) => setTimeout(resolve, 5000)); // avoid ExUI rate limiting.
    await this.continueOn(page);
  },
};

export default uploadDocumentsPage;
