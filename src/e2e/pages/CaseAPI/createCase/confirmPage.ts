import { AxeUtils } from "@hmcts/playwright-common";
import { Page } from "@playwright/test";
import confirm_content from "../../../fixtures/content/CaseAPI/createCase/confirm_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ConfirmPage = {
  closeAndReturn: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  returnCaseNumber(page: Page): Promise<string>;
  closeAndReturnToCase(page: Page): Promise<void>;
};

const createCaseConfirmPage: ConfirmPage = {
  closeAndReturn: ".button",

  async checkPageLoads(page, accessibilityTest): Promise<void> {
    await page.waitForSelector(
      `.heading-h1:text-is("${confirm_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h1:text-is("${confirm_content.subTitle1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h2:text-is("${confirm_content.textOnPage1}")`),
        1,
      ),
    ]);
    const caseElement = await page.$$("markdown > h2");

    const caseElementLength16 = await Promise.all(
      caseElement.map(async (element) => {
        const text = await page.evaluate(
          (element) => element.textContent,
          element,
        );
        if (text && text.trim().length === 16) {
          // Check if text is not null
          return element;
        }
      }),
    );

    const filteredCaseElement = caseElementLength16.filter(
      (element) => element !== null,
    );

    if (!(filteredCaseElement.length > 0)) {
      console.log("Invalid case reference.");
      process.exit(1);
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async returnCaseNumber(page: Page): Promise<string> {
    try {
      let cicCaseData: string =
        (await page.textContent("h2:nth-child(3)")) ?? "Empty";
      cicCaseData = cicCaseData.replace(/\D/g, "");
      cicCaseData = cicCaseData.replace(/(\d{4})/g, "$1-");
      cicCaseData = cicCaseData.slice(0, -1);
      return cicCaseData;
    } catch (error) {
      console.error(
        "Error occurred with capturing the case number reference.",
        error,
      );
      throw error;
    }
  },

  async closeAndReturnToCase(page: Page): Promise<void> {
    await page.locator(this.closeAndReturn).click();
    await page.waitForSelector(`h2:text-is("History")`);
    await page.waitForSelector(`.mat-tab-label-content:text-is("Tasks")`);
  },
};

export default createCaseConfirmPage;
