import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import cancelHearingReasonContent from "../../../fixtures/content/CaseAPI/cancelHearing/cancelHearingReason_content.ts";
import commonHelpers, {
  hearingCancelledReasons,
} from "../../../helpers/commonHelpers.ts";

type CancelHearingReasonPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    reasonCancelled: hearingCancelledReasons,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const cancelHearingReasonPage: CancelHearingReasonPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${cancelHearingReasonContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        cancelHearingReasonContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        cancelHearingReasonContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 9 }, (_, index) => {
        const textOnPage = (cancelHearingReasonContent as any)[
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
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(
    page: Page,
    reasonCancelled: hearingCancelledReasons,
  ): Promise<void> {
    await page.getByLabel(reasonCancelled, { exact: true }).click();
    await page.fill(
      "#cancelHearingAdditionalDetail",
      cancelHearingReasonContent.otherImportantInformation,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        cancelHearingReasonContent.errorBanner,
      ),
      expect(page.locator(".error-message")).toHaveText(
        cancelHearingReasonContent.reasonError,
      ),
    ]);
  },
};

export default cancelHearingReasonPage;
