import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import submitContent from "../../../fixtures/content/CaseAPI/documentManagementUpload/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    multipleDocuments: boolean,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(page: Page, multipleDocuments: boolean): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: 'button[name="Previous"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    multipleDocuments: boolean,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submitContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3").first()).toContainText(
        `${subjectName}`,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submitContent.caseReference + caseNumber,
      ),
      expect(page.locator("form > div > h2")).toHaveText(
        submitContent.subTitle1,
      ),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      commonHelpers.checkForButtons(
        page,
        this.saveAndContinue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (!multipleDocuments) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.complex-panel-title:has-text("${submitContent.textOnPage2}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${submitContent.textOnPage3}")`),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${submitContent.textOnPage4}")`),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${submitContent.textOnPage5}")`),
          1,
        ),
      ]);
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.complex-panel-title:has-text("${submitContent.textOnPage2}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${submitContent.textOnPage3}")`),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${submitContent.textOnPage4}")`),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${submitContent.textOnPage5}")`),
          3,
        ),
      ]);
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(page: Page, multipleDocuments: boolean): Promise<void> {
    if (!multipleDocuments) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-fixed-list-field > span.text-16:text-is("TG - Other")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-text-area-field > span:text-is("Lorem ipsum text TG - Other")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-document-field > button:text-is("${path.basename(config.testPdfFile)}")`,
          ),
          1,
        ),
      ]);
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-fixed-list-field > span.text-16:text-is("TG - Other")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-text-area-field > span:text-is("Lorem ipsum text TG - Other")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-document-field > button:text-is("${path.basename(config.testPdfFile)}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-document-field > button:text-is("${path.basename(config.testWordFile)}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-field-read-label > div > ccd-read-document-field > button:text-is("${path.basename(config.testFile)}")`,
          ),
          1,
        ),
      ]);
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
