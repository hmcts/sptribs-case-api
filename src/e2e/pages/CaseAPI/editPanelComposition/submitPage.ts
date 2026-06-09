import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import casePanelComposition_content from "../../../fixtures/content/CaseAPI/editPanelComposition/casePanelComposition_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/editPanelComposition/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { Panel2, Panel3 } from "./casePanelCompositionPage.ts";

type SubmitPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    panel2: Panel2,
    panel3: Panel3,
    specialisms: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    panel2: Panel2,
    panel3: Panel3,
    specialisms: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(`h1:text-is("${submit_content.pageTitle}")`);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage2}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage6}")`),
        1,
      ),
    ]);
    if (panel2 !== null) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage3}")`),
        1,
      );
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${panel2}")`),
        1,
      );
    }
    if (panel3 !== null) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage4}")`),
        1,
      );
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${panel3}")`),
        1,
      );
    }
    if (specialisms) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage5}")`),
        1,
      );
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span:text-is("Lorem ipsum ${casePanelComposition_content.textOnPage2}")`,
        ),
        1,
      );
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.locator(`.button:text-is("Save and continue")`).click();
  },
};

export default submitPage;
