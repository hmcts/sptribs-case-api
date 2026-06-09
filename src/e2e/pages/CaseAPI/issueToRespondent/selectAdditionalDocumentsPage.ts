import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import selectAdditionalDocuments_content from "../../../fixtures/content/CaseAPI/issueToRespondent/selectAdditionalDocuments_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SelectAdditionalDocumentsPage = {
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const selectAdditionalDocuments: SelectAdditionalDocumentsPage = {
  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${selectAdditionalDocuments_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        selectAdditionalDocuments_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        selectAdditionalDocuments_content.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toContainText(
        path.basename(config.testPdfFile) + " A - Application Form",
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.locator(".form-control").click();
    await page.getByRole("button", { name: "Continue" }).click();
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.getByRole("button", { name: "Continue" }).click();
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-heading:has-text("${selectAdditionalDocuments_content.errorTitle}")`,
      ),
      1,
    );
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-list:has-text("${selectAdditionalDocuments_content.errorMessage}")`,
      ),
      1,
    );
  },
};

export default selectAdditionalDocuments;
