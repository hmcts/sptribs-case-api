import { expect, Page } from "@playwright/test";
import editListingRegionInfoContent from "../../../fixtures/content/CaseAPI/editListing/editListingRegionInfo_content.ts";
import commonHelpers, {
  caseRegionCode,
} from "../../../helpers/commonHelpers.ts";

type EditListingRegionInfoPage = {
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

const editListingRegionInfoPage: EditListingRegionInfoPage = {
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
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/caseworker-edit-record-listing/caseworker-edit-record-listingregionInfo`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editListingRegionInfoContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        editListingRegionInfoContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editListingRegionInfoContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        editListingRegionInfoContent.textOnPage,
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
    await expect(page.locator(this.region)).toContainText("2-Midlands");
    if (region) {
      await page.selectOption(this.region, caseRegionCode);
    } else {
      await page.selectOption(this.region, { value: "0: null" });
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default editListingRegionInfoPage;
