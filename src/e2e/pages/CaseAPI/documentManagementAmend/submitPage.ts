import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import amendCaseDocuments_content from "../../../fixtures/content/CaseAPI/documentManagementAmend/amendCaseDocuments_content.ts";
import selectCaseDocuments_content from "../../../fixtures/content/CaseAPI/documentManagementAmend/selectCaseDocuments_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/documentManagementAmend/submit_content.ts";
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
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary[disabled]",
  cancel: ".cancel",
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submit_content.pageHint}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h2")).toHaveText(submit_content.pageTitle),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 1}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `text="DOC-MGMT--${path.basename(config.testPdfFile)}--${selectCaseDocuments_content.category}"`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`text="${submit_content.textOnPage5}"`),
        3,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`text="${amendCaseDocuments_content.category}"`),
        1,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },
  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
