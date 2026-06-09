import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import referCaseToLegalOfficerAdditionalInfoContent from "../../../fixtures/content/CaseAPI/referCaseToLegalOfficer/referCaseToLegalOfficerAdditionalInfo_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ReferCaseToLegalOfficerAdditionalInfoPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const referCaseToLegalOfficerAdditionalInfoPage: ReferCaseToLegalOfficerAdditionalInfoPage =
  {
    continue: '[type="submit"]',
    previous: ".button-secondary",
    cancel: ".cancel",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForURL(
        `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/refer-to-legal-officer/refer-to-legal-officerreferToLegalOfficerAdditionalInfo`,
        { timeout: 30_000 },
      );
      await Promise.all([
        expect(page.locator(".govuk-heading-l")).toHaveText(
          referCaseToLegalOfficerAdditionalInfoContent.pageHint,
        ),
        expect(page.locator("markdown > h3")).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          referCaseToLegalOfficerAdditionalInfoContent.caseReference +
            caseNumber,
        ),
        expect(page.locator(".form-label")).toHaveText(
          referCaseToLegalOfficerAdditionalInfoContent.textOnPage,
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

    async fillFields(page: Page): Promise<void> {
      await page.fill(
        "#referToLegalOfficerAdditionalInformation",
        referCaseToLegalOfficerAdditionalInfoContent.additionalInfo,
      );
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default referCaseToLegalOfficerAdditionalInfoPage;
