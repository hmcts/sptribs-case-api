import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import selectReason_content from "../../../fixtures/content/CaseAPI/closeCase/selectReason_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type CaseCloseReason =
  | "caseWithdrawn"
  | "caseRejected"
  | "caseStrikeOut"
  | "caseConcession"
  | "consentOrder"
  | "rule27"
  | "deathOfAppellant";

type SelectReasonPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(
    page: Page,
    closeReason: CaseCloseReason,
    optionalText: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const selectReasonPage: SelectReasonPage = {
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
      `.govuk-heading-l:text-is("${selectReason_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${selectReason_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${selectReason_content.textOnPage1}")`,
        ),
        1,
      ),
      ...Array.from({ length: 7 }, (_, index: number) => {
        const textOnPage = (selectReason_content as any)[
          `textOnPage${index + 2}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(
    page: Page,
    closeReason: CaseCloseReason,
    optionalText: boolean,
  ): Promise<void> {
    await page.click(`#closeCloseCaseReason-${closeReason}`);
    if (optionalText) {
      await page.fill(
        `#closeAdditionalDetail`,
        `${selectReason_content.optionalText}`,
      );
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${selectReason_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${selectReason_content.errorMessage}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${selectReason_content.errorMessage}")`,
        ),
        1,
      ),
    ]);
    await this.continueOn(page, "caseWithdrawn", false);
  },
};

export default selectReasonPage;
