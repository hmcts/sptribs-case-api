import { expect, Page } from "@playwright/test";
import rejectionDetails_content from "../../../fixtures/content/CaseAPI/closeCase/rejectionDetails_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type RejectionReason =
  | "createdInError"
  | "deadlineMissed"
  | "duplicateCase"
  | "vexatiousLitigant"
  | "other";

type RejectionDetailsPage = {
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
  continueOn(page: Page, rejectionReason: RejectionReason): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const rejectionDetailsPage: RejectionDetailsPage = {
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
      await page.click(`#closeRejectionReason-other`);
    }
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${rejectionDetails_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${rejectionDetails_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 5 }, (_, index: number) => {
        const textOnPage = (rejectionDetails_content as any)[
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
    rejectionReason: RejectionReason,
  ): Promise<void> {
    await page.click(`#closeRejectionReason-${rejectionReason}`);
    if (rejectionReason === "other") {
      await page.fill(
        `#closeRejectionDetails`,
        rejectionDetails_content.otherText,
      );
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${rejectionDetails_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${rejectionDetails_content.errorNoInput}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${rejectionDetails_content.errorNoInput}")`,
        ),
        1,
      ),
    ]);
    await page.click(`#closeRejectionReason-other`);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${rejectionDetails_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${rejectionDetails_content.errorNoOther}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${rejectionDetails_content.errorNoOther}")`,
        ),
        1,
      ),
    ]);
    await page.click(this.previous);
  },
};

export default rejectionDetailsPage;
