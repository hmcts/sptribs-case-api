import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import config from "../../../config.ts";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import decisionUpload_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/decisionUpload_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type DecisionUploadPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const decisionUploadPage: DecisionUploadPage = {
  previous: `.button-secondary:text-is("Previous")`,
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${decisionUpload_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        decisionUpload_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(
        caseSubjectDetailsObject_content.name,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        decisionUpload_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:text-is("${decisionUpload_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:text-is("${decisionUpload_content.subTitle1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:text-is("${decisionUpload_content.subTitle1}")`),
        1,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (decisionUpload_content as any)[
          `textOnPage${index + 2}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`li:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:text-is("${decisionUpload_content.textOnPage4}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h2:text-is("${decisionUpload_content.subTitle2}")`),
        1,
      ),

      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (decisionUpload_content as any)[
          `textOnPage${index + 5}`
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

  async fillInFields(page: Page): Promise<void> {
    await page.fill(`textarea`, `${decisionUpload_content.description}`);
    await page
      .locator(`#caseIssueFinalDecisionDocument_documentLink`)
      .setInputFiles(config.testPdfFile);
    await page.locator(".error-message").waitFor({ state: "hidden" });
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${decisionUpload_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${decisionUpload_content.errorNoEntryDescription}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${decisionUpload_content.errorNoEntryDescription}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${decisionUpload_content.errorNoEntryFile}")`,
        ),
        1,
      ),
    ]);
    await new Promise((resolve) => setTimeout(resolve, 5000)); // Handle ExUI rate limiting.
    await page
      .locator(`#caseIssueFinalDecisionDocument_documentLink`)
      .setInputFiles(config.testOdtFile);
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-message:has-text("${decisionUpload_content.errorInvalidFile}")`,
      ),
      1,
    );
    await page.click(this.previous);
  },
};

export default decisionUploadPage;
