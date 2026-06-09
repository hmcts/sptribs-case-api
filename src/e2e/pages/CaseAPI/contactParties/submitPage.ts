import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import partiesToContact_content from "../../../fixtures/content/CaseAPI/contactParties/partiesToContact_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/contactParties/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  change: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(page: Page, user: string): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",
  change: 'span.text-16[aria-label^="Change"]',

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    const pageHintRegex = new RegExp(
      `${submit_content.title}|${partiesToContact_content.pageHintCICA}`,
    );
    await page.waitForSelector(
      `span.text-16:text-is("${submit_content.textOnPage1}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(pageHintRegex),
      expect(page.locator("markdown > h3")).toHaveText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toHaveText(
        partiesToContact_content.caseReference + caseNumber,
      ),
      expect(page.locator(".heading-h2").nth(0)).toHaveText(
        submit_content.subTitle,
      ),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submit_content.textOnPage1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("${submit_content.textOnPage2}")`),
        4,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${partiesToContact_content.textOnPage6}")`,
        ),
        1,
      ),
      expect(page.locator("span.text-16").nth(1)).toHaveText(
        submit_content.textOnPage4,
      ),
      commonHelpers.checkForButtons(
        page,
        this.saveAndContinue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(page: Page, user: string): Promise<void> {
    const textToCheck =
      user === "respondent"
        ? partiesToContact_content.textOnPage7
        : partiesToContact_content.textOnPage4;
    await Promise.all([
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (partiesToContact_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    commonHelpers.checkVisibleAndPresent(
      page.locator(`span.text-16:text-is("${textToCheck}")`),
      1,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
