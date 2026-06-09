import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import addStay_content from "../../../fixtures/content/CaseAPI/createEditStay/addStay_content.ts";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type StayReason =
  | "waitingOutcomeOfCivilCase"
  | "awaitingOutcomeOfCriminalProceedings"
  | "awaitingACourtJudgement"
  | "unableToProgressDueToSubject"
  | "unableToProgressAsSubjectUndergoingOrAwaitingTreatment"
  | "awaitingOutcomeOfLinkedCase"
  | "Other";

type AddStayPage = {
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
    stayReason: StayReason,
    optionalText: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const addStayPage: AddStayPage = {
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
      `.govuk-heading-l:text-is("${addStay_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        addStay_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 13 }, (_, index: number) => {
        const textOnPage = (addStay_content as any)[`textOnPage${index + 1}`];
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
    stayReason: StayReason,
    optionalText: Boolean,
  ): Promise<void> {
    if (stayReason !== "unableToProgressDueToSubject") {
      await page.click(`#stayStayReason-${stayReason}`);
    } else {
      await page.click(`#stayStayReason-${stayReason}’sAge`);
    }
    if (stayReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.form-label:text-is("${addStay_content.textOnPage14}")`),
        1,
      );
      await page.fill(`#stayFlagType`, addStay_content.otherText);
    }
    await page.fill(`#stayExpirationDate-day`, addStay_content.day);
    await page.fill(`#stayExpirationDate-month`, addStay_content.month);
    await page.fill(`#stayExpirationDate-year`, addStay_content.year);
    if (optionalText) {
      await page.fill(
        `#stayAdditionalDetail`,
        `${addStay_content.optionalText}`,
      );
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${addStay_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${addStay_content.errorReasonMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${addStay_content.errorReasonMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${addStay_content.errorDateMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${addStay_content.errorDateMissing}")`,
        ),
        1,
      ),
    ]);
    await page.click(`#stayStayReason-Other`);
    await new Promise((resolve) => setTimeout(resolve, 5000)); // avoid ExUI concurrency not loading
    await page.click(this.continue);
    await page.waitForSelector(
      `#error-summary-title:text-is("${addStay_content.errorBanner}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${addStay_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${addStay_content.errorOtherMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${addStay_content.errorOtherMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${addStay_content.errorDateMissing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${addStay_content.errorDateMissing}")`,
        ),
        1,
      ),
    ]);
    await this.continueOn(page, "waitingOutcomeOfCivilCase", false);
  },
};

export default addStayPage;
