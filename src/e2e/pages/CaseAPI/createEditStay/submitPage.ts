import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import addStay_content from "../../../fixtures/content/CaseAPI/createEditStay/addStay_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/createEditStay/submit_content.ts";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { StayReason } from "./addStayPage.ts";

type SubmitPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    stayReason: StayReason,
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
    stayReason: StayReason,
    optionalText: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submit_content.pageHint}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h2")).toHaveText(submit_content.pageTitle),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 3 }, (_, index: number) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 1}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    if (stayReason === "Other") {
      await expect(
        page.locator(`.text-16:text-is("${submit_content.textOnPage4}")`),
      ).toBeVisible();
    }
    if (optionalText) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage5}")`),
        1,
      );
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    stayReason: keyof typeof submit_content,
    optionalText: boolean,
  ): Promise<void> {
    const stayReasonText = submit_content[stayReason];
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${stayReasonText}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${addStay_content.day} ${await commonHelpers.shortMonths(parseInt(addStay_content.month))} ${addStay_content.year}")`,
        ),
        1,
      ),
    ]);
    if (stayReason === "Other") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${addStay_content.otherText}")`),
        1,
      );
    }
    if (optionalText) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`span:text-is("${addStay_content.optionalText}")`),
        1,
      );
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
