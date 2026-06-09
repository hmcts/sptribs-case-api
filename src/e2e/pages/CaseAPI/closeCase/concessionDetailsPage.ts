import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import concessionDetails_content from "../../../fixtures/content/CaseAPI/closeCase/concessionDetails_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConcessionDetailsPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const concessionDetailsPage: ConcessionDetailsPage = {
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
      `.govuk-heading-l:text-is("${concessionDetails_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.govuk-caption-l:text-is("${concessionDetails_content.pageHint}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (concessionDetails_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      await commonHelpers.checkForButtons(
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

  async continueOn(page: Page): Promise<void> {
    await page.fill(
      `#closeConcessionDate-day`,
      `${concessionDetails_content.day}`,
    );
    await page.fill(
      `#closeConcessionDate-month`,
      `${concessionDetails_content.month}`,
    );
    await page.fill(
      `#closeConcessionDate-year`,
      `${concessionDetails_content.year}`,
    );
    // Workaround to remove the date error which stops the continue button from being clicked
    await page.locator("h1").click();
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${concessionDetails_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${concessionDetails_content.errorConceded}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${concessionDetails_content.errorConceded}")`,
        ),
        1,
      ),
    ]);
    await page.click(this.previous);
  },
};

export default concessionDetailsPage;
