import { expect, Page } from "@playwright/test";
import strikeoutDetails_content from "../../../fixtures/content/CaseAPI/closeCase/strikeoutDetails_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type StrikeoutReason = "noncomplianceWithDirections" | "other";

type StrikeoutDetailsPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    errorMessaging: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page, strikeoutReason: StrikeoutReason): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const strikeoutDetailsPage: StrikeoutDetailsPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    errorMessaging: boolean,
    subjectName: string,
  ): Promise<void> {
    if (!errorMessaging) {
      await page.click(`#closeStrikeOutReason-other`);
    }
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${strikeoutDetails_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${strikeoutDetails_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (strikeoutDetails_content as any)[
          `textOnPage${index + 1}`
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
      // await new AxeUtils(page).audit();
    }
  },

  async continueOn(
    page: Page,
    strikeoutReason: StrikeoutReason,
  ): Promise<void> {
    await page.click(`#closeStrikeOutReason-${strikeoutReason}`);
    if (strikeoutReason === "other") {
      await page.fill(
        `#closeStrikeOutDetails`,
        strikeoutDetails_content.otherText,
      );
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${strikeoutDetails_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${strikeoutDetails_content.errorStrikeout}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${strikeoutDetails_content.errorStrikeout}")`,
        ),
        1,
      ),
    ]);
    await page.click(`#closeStrikeOutReason-other`);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${strikeoutDetails_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${strikeoutDetails_content.errorAdditionalInfo}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${strikeoutDetails_content.errorAdditionalInfo}")`,
        ),
        1,
      ),
    ]);
    await page.click(this.previous);
  },
};

export default strikeoutDetailsPage;
