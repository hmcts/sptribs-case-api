import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import cicaCaseDetails_content from "../../../fixtures/content/CaseAPI/createCase/cicaCaseDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CicaCaseDetailsPage = {
  continue: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(page: Page): Promise<void>;
};

const cicaCaseDetailsPage: CicaCaseDetailsPage = {
  continue: '[type="submit"]',

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${cicaCaseDetails_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        cicaCaseDetails_content.pageHint,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (cicaCaseDetails_content as any)[
          `textOnPage${index + 1}`
        ];
      }),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default cicaCaseDetailsPage;
