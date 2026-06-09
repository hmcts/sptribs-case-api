import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import stateTabContent from "../../../fixtures/content/CaseAPI/caseTabs/stateTab_content.ts";
import states_content from "../../../fixtures/content/states_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type StateTabPage = {
  caseStateTab: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  changeToStateTab(page: Page): Promise<void>;
  checkStateTab(page: Page, state: string): Promise<void>;
};

const stateTabPage: StateTabPage = {
  caseStateTab: `.mat-tab-label-content:text-is("State")`,

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await commonHelpers.checkAllCaseTabs(page, caseNumber, false, subjectName);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async changeToStateTab(page: Page): Promise<void> {
    await page.locator(this.caseStateTab).click();
  },

  async checkStateTab(page: Page, state: string): Promise<void> {
    await page.waitForSelector(
      `markdown[class='markdown'] h4:has-text("${stateTabContent.caseState}")`,
    );
    if (state == states_content.DSSSubmittedState) {
      await expect(page.locator("markdown[class='markdown'] h4")).toHaveText(
        stateTabContent.caseState + states_content.DSSSubmittedState,
      );
    } else if (state == states_content.submittedState) {
      await expect(page.locator("markdown[class='markdown'] h4")).toHaveText(
        stateTabContent.caseState + states_content.submittedState,
      );
    } else if (state == states_content.caseManagementState) {
      await expect(page.locator("markdown[class='markdown'] h4")).toHaveText(
        stateTabContent.caseState + states_content.caseManagementState,
      );
    } else if (state == states_content.closedState) {
      await expect(page.locator("markdown[class='markdown'] h4")).toHaveText(
        stateTabContent.caseState + states_content.closedState,
      );
    }
  },
};

export default stateTabPage;
