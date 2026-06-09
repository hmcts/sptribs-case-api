import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import removeStay_content from "../../../fixtures/content/CaseAPI/removeStay/removeStay_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/removeStay/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { RemoveReason } from "./removeStayPage.ts";

type SubmitPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    removeReason: RemoveReason,
    optionalText: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    stayReason: keyof typeof submit_content,
    optionalText: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    removeReason: RemoveReason,
    optionalText: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h2:text-is("${submit_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        submit_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 1}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    if (removeReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage3}")`),
        1,
      );
    }
    if (optionalText) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage4}")`),
        1,
      );
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    removeReason: keyof typeof submit_content,
    optionalText: boolean,
  ): Promise<void> {
    const removeReasonText = submit_content[removeReason];
    await commonHelpers.checkVisibleAndPresent(
      page.locator(`.text-16:text-is("${removeReasonText}")`),
      1,
    );
    if (removeReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${removeStay_content.otherText}")`),
        1,
      );
    }
    if (optionalText) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`span:text-is("${removeStay_content.optionalText}")`),
        1,
      );
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
