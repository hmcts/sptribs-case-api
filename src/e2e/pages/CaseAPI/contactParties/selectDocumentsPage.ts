import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import partiesToContact_content from "../../../fixtures/content/CaseAPI/contactParties/partiesToContact_content.ts";
import selectDocument_content from "../../../fixtures/content/CaseAPI/contactParties/selectDocument_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SelectDocumentsPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  tickCheckbox(page: Page, taskCompletion: boolean): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const selectDocumentsPage: SelectDocumentsPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary[disabled]",
  cancel: ".cancel",
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    const pageHintRegex = new RegExp(
      `${selectDocument_content.pageHint}|${partiesToContact_content.pageHintCICA}`,
    );
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${selectDocument_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(pageHintRegex),
      expect(page.locator("markdown > h3").nth(0)).toHaveText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toHaveText(
        partiesToContact_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const textOnPage = (selectDocument_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`markdown > p:text-is("${textOnPage}")`),
          1,
        );
      }),
      expect(page.locator(".form-label").nth(0)).toHaveText(
        selectDocument_content.textOnPage3,
      ),
      commonHelpers.checkVisibleAndPresent(
        page
          .locator(
            `markdown > p:text-is("${path.basename(config.testPdfFile)} ${selectDocument_content.category}")`,
          )
          .first(),
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
  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
  async tickCheckbox(page: Page, taskCompletion: boolean): Promise<void> {
    if (taskCompletion) {
      await page
        .locator(
          'input[type="checkbox"][name="contactPartiesDocumentsDocumentList"][id^="contactPartiesDocumentsDocumentList_"]',
        )
        .nth(1)
        .check();
      const isChecked = await page
        .locator(
          'input[type="checkbox"][id^="contactPartiesDocumentsDocumentList_"]',
        )
        .nth(1)
        .isChecked();
    } else {
      await page
        .locator(
          'input[type="checkbox"][name="contactPartiesDocumentsDocumentList"][id^="contactPartiesDocumentsDocumentList_"]',
        )
        .nth(0)
        .check();
      const isChecked = await page
        .locator(
          'input[type="checkbox"][id^="contactPartiesDocumentsDocumentList_"]',
        )
        .nth(0)
        .isChecked();
    }
  },
};

export default selectDocumentsPage;
