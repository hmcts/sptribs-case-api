import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import selectTemplate_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/selectTemplate_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type Template =
  | "--Select a value--"
  | "CIC1 - Eligibility"
  | "CIC2 - Quantum"
  | "CIC3 - Rule 27"
  | "CIC4 - Blank Decision Notice"
  | "CIC6 - General Directions"
  | "CIC7 - ME Dmi Reports"
  | "CIC8 - ME Joint Instructions"
  | "CIC8 - ME Joint Instruction"
  | "CIC10 - Strike Out Warning"
  | "CIC11 - Strike Out Decision Notice"
  | "CIC13 - Pro Forma Summons"
  | "CIC14 – LO General Directions"
  | null; // for template upload.

type SelectTemplatePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void>;
  fillInFields(page: Page, template: Template): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const selectTemplatePage: SelectTemplatePage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${selectTemplate_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        selectTemplate_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(
        caseSubjectDetailsObject_content.name,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        selectTemplate_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${selectTemplate_content.textOnPage1}")`,
        ),
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

  async fillInFields(page: Page, template: Template): Promise<void> {
    await page.selectOption(
      `#caseIssueFinalDecisionDecisionTemplate`,
      template,
    );
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${selectTemplate_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${selectTemplate_content.errorNoEntry}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${selectTemplate_content.errorNoEntry}")`,
        ),
        1,
      ),
    ]);
    await this.fillInFields(page, "CIC4 - Blank Decision Notice");
  },
};

export default selectTemplatePage;
