import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import addDocumentFooterPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/addDocumentFooterPage_content.ts";
import orderMainContentPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/orderMainContentPage_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { Template } from "../issueFinalDecision/selectTemplatePage.ts";

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
  checkAllInfo(page: Page, template: Template): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary",
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
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage1}")`),
        1,
      ),
      ...Array.from({ length: 6 }, (_, index) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page
            .locator(`.text-16:text-is("${textOnPage}")`)
            .filter({ visible: true }),
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

  async checkAllInfo(page: Page, template: Template): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${template}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span:text-is("${orderMainContentPage_content.description}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${addDocumentFooterPage_content.signature}")`,
        ),
        1,
      ),
    ]);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
