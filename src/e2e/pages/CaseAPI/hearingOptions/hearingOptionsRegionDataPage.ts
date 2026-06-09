import { expect, Page } from "@playwright/test";
import addCaseNotes_content from "../../../fixtures/content/CaseAPI/addNote/addCaseNotes_content.ts";
import hearingOptionsRegionDataContent from "../../../fixtures/content/CaseAPI/hearingOptions/hearingOptionsRegionData_content.ts";
import commonHelpers, {
  caseRegionCode,
} from "../../../helpers/commonHelpers.ts";

type HearingOptionsRegionDataPage = {
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

const hearingOptionsRegionData: HearingOptionsRegionDataPage = {
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
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${hearingOptionsRegionDataContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p")).toContainText(
        addCaseNotes_content.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        hearingOptionsRegionDataContent.textOnPage,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      // await new AxeUtils(page).audit();
    }
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
    await page.locator(`h1:text-is("Case: Hearing Options")`);
  },
};

export default hearingOptionsRegionData;
