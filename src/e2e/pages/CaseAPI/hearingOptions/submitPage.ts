import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import addCaseNotes_content from "../../../fixtures/content/CaseAPI/addNote/addCaseNotes_content.ts";
import hearingOptionsHearingDetailsContent from "../../../fixtures/content/CaseAPI/hearingOptions/hearingOptionsHearingDetails_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/hearingOptions/submit_content.ts";
import commonHelpers, {
  caseRegionCode,
  hearingFormat,
} from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    region: boolean,
    venue: boolean,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    venue: boolean,
    venueNotListed: boolean,
    hearingFormat: hearingFormat,
    shortNoticeHearing: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    region: boolean,
    venue: boolean,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submitContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p")).toContainText(
        addCaseNotes_content.caseReference + caseNumber,
      ),
      expect(page.locator(".text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 4}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      commonHelpers.checkForButtons(
        page,
        this.saveAndContinue,
        this.previous,
        this.cancel,
      ),
    ]);
    if (region) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage2}")`,
        ),
        1,
      );
    }
    if (venue) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage3}")`,
        ),
        1,
      );
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    venue: boolean,
    venueNotListed: boolean,
    hearingFormat: hearingFormat,
    shortNoticeHearing: boolean,
  ): Promise<void> {
    if (region) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-dynamic-list-field > span.text-16:text-is("${caseRegionCode}")`,
        ),
        1,
      );
    }
    if (venue) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-dynamic-list-field > span.text-16:text-is("${hearingOptionsHearingDetailsContent.venue}")`,
        ),
        1,
      );
    }
    if (venueNotListed) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingOptionsHearingDetailsContent.textOnPage2}")`,
        ),
        1,
      );
    }
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${hearingOptionsHearingDetailsContent.room}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${hearingOptionsHearingDetailsContent.instructions}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingFormat}")`,
        ),
        1,
      ),
    ]);
    if (shortNoticeHearing) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`ccd-read-yes-no-field > span.text-16:text-is("Yes")`),
        1,
      );
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`ccd-read-yes-no-field > span.text-16:text-is("No")`),
        1,
      );
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
    await page.waitForSelector(`h2:text-is("History")`);
    await page.waitForSelector(`.mat-tab-label-content:text-is("Tasks")`);
  },
};

export default submitPage;
