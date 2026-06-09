import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editListingHearingTypeAndFormatContent from "../../../fixtures/content/CaseAPI/editListing/editListingHearingTypeAndFormat_content.ts";
import commonHelpers, {
  hearingFormat,
  hearingType,
} from "../../../helpers/commonHelpers.ts";

type EditListingHearingTypeAndFormatPage = {
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

const editListingHearingTypeAndFormatPage: EditListingHearingTypeAndFormatPage =
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
        `.govuk-heading-l:text-is("${editListingHearingTypeAndFormatContent.pageTitle}")`,
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          editListingHearingTypeAndFormatContent.pageHint,
        ),
        expect(page.locator("markdown > h3")).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          editListingHearingTypeAndFormatContent.caseReference + caseNumber,
        ),
        ...Array.from({ length: 10 }, (_, index) => {
          const textOnPage = (editListingHearingTypeAndFormatContent as any)[
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
      await expect(page.getByLabel("Final")).toBeChecked();
      await expect(page.getByLabel("Paper")).toBeChecked();
      await page.getByLabel(hearingType).check();
      await page.getByLabel(hearingFormat).check();
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default editListingHearingTypeAndFormatPage;
