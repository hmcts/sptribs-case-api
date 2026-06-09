import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import submit_content from "../../../fixtures/content/CaseAPI/issueToRespondent/submit_content.ts";
import commonHelpers, { parties } from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    recipients: parties[],
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page, recipients: string[]): Promise<void>;
};

const submitPage: SubmitPage = {
  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    recipients: parties[],
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submit_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h2")).toHaveText(
        submit_content.pageSubtitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`div > span:text-is("${submit_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`th > span:text-is("${submit_content.textOnPage2}")`),
        recipients.length,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("Change")`),
        recipients.length + 1,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(page: Page, recipients: parties[]): Promise<void> {
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `p > a:text-is("${path.basename(config.testPdfFile)}  A - Application Form")`,
      ),
      1,
    );
    if (recipients.includes("Subject")) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`td > span:text-is("${submit_content.textOnPage3}")`),
        1,
      );
    }
    if (recipients.includes("Representative")) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`td > span:text-is("${submit_content.textOnPage4}")`),
        1,
      );
    }
    if (recipients.includes("Respondent")) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`td > span:text-is("${submit_content.textOnPage5}")`),
        1,
      );
    }
    if (recipients.includes("Applicant")) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`td > span:text-is("${submit_content.textOnPage6}")`),
        1,
      );
    }
    await page.getByRole("button", { name: "Save and continue" }).click();
  },
};

export default submitPage;
