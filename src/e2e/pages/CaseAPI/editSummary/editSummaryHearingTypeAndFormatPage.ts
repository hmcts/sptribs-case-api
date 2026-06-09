import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editSummaryHearingTypeAndFormatContent from "../../../fixtures/content/CaseAPI/editSummary/editSummaryHearingTypeAndFormat_content.ts";
import commonHelpers, {
  hearingFormat,
  hearingType,
} from "../../../helpers/commonHelpers.ts";

type EditSummaryHearingTypeAndFormatPage = {
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
    hearingType: hearingType,
    hearingFormat: hearingFormat,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editSummaryHearingTypeAndFormatPage: EditSummaryHearingTypeAndFormatPage =
  {
    previous: ".button-secondary[disabled]",
    continue: '[type="submit"]',
    cancel: ".cancel",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForSelector(
        `.govuk-heading-l:text-is("${editSummaryHearingTypeAndFormatContent.pageTitle}")`,
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          editSummaryHearingTypeAndFormatContent.pageHint,
        ),
        expect(page.locator("markdown > h3")).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          editSummaryHearingTypeAndFormatContent.caseReference + caseNumber,
        ),
        ...Array.from({ length: 10 }, (_, index) => {
          const textOnPage = (editSummaryHearingTypeAndFormatContent as any)[
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
      hearingType: hearingType,
      hearingFormat: hearingFormat,
    ): Promise<void> {
      await expect(page.getByLabel("Case management")).toBeChecked();
      await expect(page.getByLabel("Hybrid")).toBeChecked();
      await page.getByLabel(hearingType).check();
      await page.getByLabel(hearingFormat).check();
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default editSummaryHearingTypeAndFormatPage;
