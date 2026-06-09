import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import anonymityPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/anonymityPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ApplyAnonymityPage = {
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
  triggerErrorMessages(page: Page): Promise<void>;
};

const applyAnonymityPage: ApplyAnonymityPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${anonymityPage_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        anonymityPage_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`div > markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        anonymityPage_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 1 }, (_, index) => {
        const textOnPage = (anonymityPage_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`span.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
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
    await page.getByLabel("No", { exact: true }).click();
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await expect(page.locator(`input#orderContentOrderSignature`)).toBeEmpty();
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${anonymityPage_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${anonymityPage_content.errorNoEntry}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${anonymityPage_content.errorNoEntry}")`,
        ),
        1,
      ),
    ]);
    await this.fillInFields(page);
  },
};

export default applyAnonymityPage;
