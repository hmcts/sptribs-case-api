import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import config from "../../../config.ts";
import caseDocumentsUploadObject_content from "../../../fixtures/content/CaseAPI/createCase/caseDocumentsUploadObject_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type caseDocumentsUploadObjectPage = {
  continue: string;
  addNew: string;
  addNewBottom: string;
  remove: string;
  confirmRemove: string;
  cancelRemove: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(page: Page, multipleFiles: boolean): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const caseDocumentsUploadObjectPage: caseDocumentsUploadObjectPage = {
  continue: '[type="submit"]',
  addNew: ".write-collection-add-item__top",
  addNewBottom: ".write-collection-add-item__bottom",
  remove: ".button-secondary",
  confirmRemove: ".action-button",
  cancelRemove: "button-secondary",

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${caseDocumentsUploadObject_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        caseDocumentsUploadObject_content.pageHint,
      ),
      expect(page.locator("[class='markdown'] p")).toHaveText(
        caseDocumentsUploadObject_content.textOnPage1,
      ),
      expect(
        page.locator("body exui-root exui-case-create-submit li:nth-child(1)"),
      ).toHaveText(caseDocumentsUploadObject_content.textOnPage2),
      expect(
        page.locator("body exui-root exui-case-create-submit li:nth-child(2)"),
      ).toHaveText(caseDocumentsUploadObject_content.textOnPage3),
      expect(
        page.locator("body exui-root exui-case-create-submit li:nth-child(3)"),
      ).toHaveText(caseDocumentsUploadObject_content.textOnPage4),
      expect(page.locator(".heading-h2").nth(0)).toHaveText(
        caseDocumentsUploadObject_content.subSubTitle1,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page, multipleFiles: boolean): Promise<void> {
    await page.click(this.addNew);
    await commonHelpers.uploadFileController(
      page,
      "cicCaseCaseDocumentsUpload",
      0,
      "A - Application Form",
      config.testPdfFile,
      false,
    );
    if (multipleFiles) {
      await page.click(this.addNewBottom);
      await commonHelpers.uploadFileController(
        page,
        "cicCaseCaseDocumentsUpload",
        1,
        "A - Application Form",
        config.testWordFile,
        false,
      );
      await page.click(this.addNewBottom);
      await commonHelpers.uploadFileController(
        page,
        "cicCaseCaseDocumentsUpload",
        2,
        "A - Application Form",
        config.testFile,
        false,
      );
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        caseDocumentsUploadObject_content.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        caseDocumentsUploadObject_content.fileError,
      ),
      expect(page.locator(".error-message")).toHaveText(
        caseDocumentsUploadObject_content.fileError,
      ),
    ]);
    await page.click(this.addNew);
    await page.click(this.continue);
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        caseDocumentsUploadObject_content.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        caseDocumentsUploadObject_content.categoryError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        caseDocumentsUploadObject_content.descriptionError,
      ),
      expect(page.locator(".validation-error").nth(2)).toHaveText(
        caseDocumentsUploadObject_content.fieldError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        caseDocumentsUploadObject_content.categoryError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        caseDocumentsUploadObject_content.descriptionError,
      ),
    ]);
    await page.click(this.remove);
    await expect(page.locator(".cdk-overlay-container")).toBeVisible();
    await page.click(this.confirmRemove);
    await page.waitForTimeout(500);
  },
};

export default caseDocumentsUploadObjectPage;
