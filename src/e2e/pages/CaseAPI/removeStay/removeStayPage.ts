import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import removeStay_content from "../../../fixtures/content/CaseAPI/removeStay/removeStay_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type RemoveReason =
  | "receivedOutcomeOfCivilCase"
  | "receviedOutcomeOfCriminalProceedings"
  | "receivedACourtJudgement"
  | "applicantHasReachedRequiredAge"
  | "subjectHasReceivedTheirMedicalTreatment"
  | "receivedOutcomeOfLinkedCase"
  | "Other";

type RemoveStayPage = {
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
    removeReason: RemoveReason,
    optionalText: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const removeStayPage: RemoveStayPage = {
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
      `.govuk-heading-l:text-is("${removeStay_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        removeStay_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 9 }, (_, index: number) => {
        const textOnPage = (removeStay_content as any)[
          `textOnPage${index + 1}`
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
    removeReason: RemoveReason,
    optionalText: boolean,
  ): Promise<void> {
    await page.click(`#removeStayStayRemoveReason-${removeReason}`);
    if (removeReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${removeStay_content.textOnPage10}")`,
        ),
        1,
      );
      await page.fill(
        `#removeStayStayRemoveOtherDescription`,
        removeStay_content.otherText,
      );
    }
    if (optionalText) {
      await page.fill(
        `#removeStayAdditionalDetail`,
        `${removeStay_content.optionalText}`,
      );
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await page.waitForSelector(
      `#error-summary-title:text-is("${removeStay_content.errorBanner}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${removeStay_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${removeStay_content.errorReasonMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${removeStay_content.errorReasonMissing}")`,
        ),
        1,
      ),
    ]);
    await page.click(`#removeStayStayRemoveReason-Other`);
    await new Promise((resolve) => setTimeout(resolve, 5000)); // avoid ExUI concurrency not loading
    await page.click(this.continue);
    await page.waitForSelector(
      `.error-message:has-text("${removeStay_content.errorOtherMissing}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${removeStay_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${removeStay_content.errorOtherMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${removeStay_content.errorOtherMissing}")`,
        ),
        1,
      ),
    ]);
    await this.continueOn(page, "receivedOutcomeOfCivilCase", false);
  },
};

export default removeStayPage;
