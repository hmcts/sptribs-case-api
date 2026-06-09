import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import noticeOption_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/noticeOption_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

export type NoticeType = "upload" | "Create";

type NoticeOptionPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void>;
  fillInFields(page: Page, noticeType: NoticeType): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const noticeOptionPage: NoticeOptionPage = {
  previous: ".button-secondary[disabled]",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${noticeOption_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        noticeOption_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(
        caseSubjectDetailsObject_content.name,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        noticeOption_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (noticeOption_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:text-is("${noticeOption_content.textOnPage4}")`),
        1,
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

  async fillInFields(page: Page, noticeType: NoticeType): Promise<void> {
    switch (noticeType) {
      case "Create":
        await page.locator(".form-control").nth(1).click();
        break;
      default: // Manual upload
        await page.locator(".form-control").nth(0).click();
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#error-summary-title:text-is("${noticeOption_content.errorBanner}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.validation-error:has-text("${noticeOption_content.errorNoEntry}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-message:has-text("${noticeOption_content.errorNoEntry}")`,
        ),
        1,
      ),
    ]);
    await this.fillInFields(page, "upload");
  },
};

export default noticeOptionPage;
