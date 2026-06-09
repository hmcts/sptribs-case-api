import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createListingOtherInformationContent from "../../../fixtures/content/CaseAPI/createListing/createListingOtherInformation_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateListingOtherInformationPage = {
  importantInfo: string;
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createListingOtherInformationPage: CreateListingOtherInformationPage = {
  importantInfo: "#importantInfoDetails",
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForURL(
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/caseworker-record-listing/caseworker-record-listingotherInformation`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        createListingOtherInformationContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        createListingOtherInformationContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingOtherInformationContent.caseReference + caseNumber,
      ),
      expect(page.locator("markdown > p").nth(1)).toHaveText(
        createListingOtherInformationContent.textOnPage1,
      ),
      expect(page.locator(".form-label")).toHaveText(
        createListingOtherInformationContent.textOnPage2,
      ),
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

  async fillInFields(page: Page): Promise<void> {
    await page.fill(
      this.importantInfo,
      createListingOtherInformationContent.otherInformation,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default createListingOtherInformationPage;
