import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import referCaseToJudgeAdditionalInfoContent from "../../../fixtures/content/CaseAPI/referCaseToJudge/referCaseToJudgeAdditionalInfo_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ReferCaseToJudgeAdditionalInfoPage = {
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

const referCaseToJudgeAdditionalInfoPage: ReferCaseToJudgeAdditionalInfoPage = {
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
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/refer-to-judge/refer-to-judgereferToJudgeAdditionalInfo`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        referCaseToJudgeAdditionalInfoContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        referCaseToJudgeAdditionalInfoContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label")).toHaveText(
        referCaseToJudgeAdditionalInfoContent.textOnPage,
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
      "#referToJudgeAdditionalInformation",
      referCaseToJudgeAdditionalInfoContent.additionalInfo,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default referCaseToJudgeAdditionalInfoPage;
