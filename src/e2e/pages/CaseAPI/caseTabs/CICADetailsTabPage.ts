import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import CICADetailsTabContent from "../../../fixtures/content/CaseAPI/caseTabs/CICADetailsTab_content.ts";
import editCICACaseDetailsEditCaseDetailsContent from "../../../fixtures/content/CaseAPI/editCICACaseDetails/editCICACaseDetailsEditCaseDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CICADetailsTabPage = {
  CICADetailsTab: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  changeToCICADetailsTab(page: Page): Promise<void>;
  checkValidInfo(page: Page): Promise<void>;
};

const cicaDetailsTabPage: CICADetailsTabPage = {
  CICADetailsTab: `.mat-tab-label-content:text-is("CICA Details")`,

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      commonHelpers.checkAllCaseTabs(page, caseNumber, true, subjectName),
      expect(page.locator("h4").first()).toContainText(
        CICADetailsTabContent.title,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (CICADetailsTabContent as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async changeToCICADetailsTab(page: Page): Promise<void> {
    await page.locator(this.CICADetailsTab).click();
  },

  async checkValidInfo(page: Page): Promise<void> {
    await Promise.all([
      expect(page.locator("ccd-read-text-field").nth(0)).toHaveText(
        editCICACaseDetailsEditCaseDetailsContent.referenceNumber,
      ),
      expect(page.locator("ccd-read-text-field").nth(1)).toHaveText(
        editCICACaseDetailsEditCaseDetailsContent.caseWorker,
      ),
      expect(page.locator("ccd-read-text-field").nth(2)).toHaveText(
        editCICACaseDetailsEditCaseDetailsContent.presentingOfficer,
      ),
    ]);
  },
};

export default cicaDetailsTabPage;
