import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editSummaryHearingOutcomeContent from "../../../fixtures/content/CaseAPI/editSummary/editSummaryHearingOutcome_content.ts";
import commonHelpers, {
  hearingAdjournedReasons,
  hearingOutcome,
} from "../../../helpers/commonHelpers.ts";

type EditSummaryHearingOutcomePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    errorMessaging: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(
    page: Page,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editSummaryHearingOutcomePage: EditSummaryHearingOutcomePage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    errorMessaging: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForURL(
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/edit-hearing-summary/edit-hearing-summaryhearingOutcome`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editSummaryHearingOutcomeContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        editSummaryHearingOutcomeContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editSummaryHearingOutcomeContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 5 }, (_, index) => {
        const textOnPage = (editSummaryHearingOutcomeContent as any)[
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
    if (!errorMessaging) {
      await page
        .getByLabel(editSummaryHearingOutcomeContent.textOnPage2, {
          exact: true,
        })
        .click();
      await page
        .getByLabel(editSummaryHearingOutcomeContent.textOnPage33, {
          exact: true,
        })
        .click();
      await Promise.all([
        expect(
          page.locator(
            "#adjournmentReasons > fieldset > legend > label > span",
          ),
        ).toHaveText(editSummaryHearingOutcomeContent.textOnPage6),
        ...Array.from({ length: 26 }, (_, index) => {
          const textOnPage = (editSummaryHearingOutcomeContent as any)[
            `textOnPage${index + 7}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#adjournmentReasons > fieldset > div > label:text-is("${textOnPage}")`,
            ),
            1,
          );
        }),
        expect(
          page.locator(
            "#caseEditForm > div > ccd-field-write > div > ccd-write-text-area-field > div > label > span",
          ),
        ).toHaveText(editSummaryHearingOutcomeContent.textOnPage34),
      ]);
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillFields(
    page: Page,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
  ): Promise<void> {
    await page.getByLabel(hearingOutcome, { exact: true }).click();
    if (hearingAdjournedReason !== null) {
      await page.getByLabel(hearingAdjournedReason, { exact: true }).click();
      if (hearingAdjournedReason === "Other") {
        await page
          .locator("#otherDetailsOfAdjournment")
          .fill(editSummaryHearingOutcomeContent.otherAdjournedReason);
      }
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page
      .getByLabel(editSummaryHearingOutcomeContent.textOnPage2, {
        exact: true,
      })
      .click();
    await page.waitForTimeout(1000);
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editSummaryHearingOutcomeContent.errorBanner,
      ),
      expect(page.locator(".error-message")).toHaveText(
        editSummaryHearingOutcomeContent.adjournedError,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        editSummaryHearingOutcomeContent.adjournedError,
      ),
    ]);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default editSummaryHearingOutcomePage;
