import { expect, Page } from "@playwright/test";
import createListingRegionInfoContent from "../../../fixtures/content/CaseAPI/createListing/createListingRegionInfo_content.ts";
import commonHelpers, {
  caseRegionCode,
} from "../../../helpers/commonHelpers.ts";

type CreateListingRegionInfoPage = {
  region: string;
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
    region: boolean,
    caseRegionCode: caseRegionCode | null,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createListingRegionInfoPage: CreateListingRegionInfoPage = {
  region: "#regionList",
  previous: ".button-secondary[disabled]",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForURL(
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/caseworker-record-listing/caseworker-record-listingregionInfo`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        createListingRegionInfoContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        createListingRegionInfoContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingRegionInfoContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        createListingRegionInfoContent.textOnPage,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    // if (accessibilityTest) {
    //   await new AxeUtils(page).audit();
    // }
  },

  async fillInFields(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
  ): Promise<void> {
    if (region) {
      await page.selectOption(this.region, caseRegionCode);
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default createListingRegionInfoPage;
