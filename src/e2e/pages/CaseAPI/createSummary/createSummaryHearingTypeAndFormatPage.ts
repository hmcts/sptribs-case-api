import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createSummaryHearingTypeAndFormatContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingTypeAndFormat_content.ts";
import commonHelpers, {
  hearingFormat,
  hearingType,
} from "../../../helpers/commonHelpers.ts";

type CreateSummaryHearingTypeAndFormatPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkFields(
    page: Page,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    editJourney: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createSummaryHearingTypeAndFormatPage: CreateSummaryHearingTypeAndFormatPage =
  {
    previous: "button[name='Previous']",
    continue: '[type="submit"]',
    cancel: ".cancel",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForSelector(
        `.govuk-heading-l:text-is("${createSummaryHearingTypeAndFormatContent.pageTitle}")`,
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          createSummaryHearingTypeAndFormatContent.pageHint,
        ),
        expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          createSummaryHearingTypeAndFormatContent.caseReference + caseNumber,
        ),
        ...Array.from({ length: 10 }, (_, index) => {
          const textOnPage = (createSummaryHearingTypeAndFormatContent as any)[
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

    async checkFields(
      page: Page,
      hearingType: hearingType,
      hearingFormat: hearingFormat,
      editJourney: boolean,
    ): Promise<void> {
      await expect(page.getByLabel(hearingType)).toBeChecked();
      await expect(page.getByLabel(hearingFormat)).toBeChecked();
      if (editJourney) {
        await page.getByLabel("Final").check();
        await page.getByLabel("Video").check();
      }
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default createSummaryHearingTypeAndFormatPage;
