import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editListingChangeReasonContent from "../../../fixtures/content/CaseAPI/editListing/editListingChangeReason_content.ts";
import editListingListingDetailsContent from "../../../fixtures/content/CaseAPI/editListing/editListingListingDetails_content.ts";
import editListingNotifyPageContent from "../../../fixtures/content/CaseAPI/editListing/editListingNotifyPage_content.ts";
import editListingOtherInformationContent from "../../../fixtures/content/CaseAPI/editListing/editListingOtherInformation_content.ts";
import editListingRemoteHearingInformationContent from "../../../fixtures/content/CaseAPI/editListing/editListingRemoteHearingInformation_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/editListing/submit_content.ts";
import commonHelpers, {
  caseRegionCode,
  hearingFormat,
  hearingSession,
  hearingType,
  hearingVenues,
} from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    region: boolean,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    hearing: string | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    region: boolean,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h2:text-is("${submitContent.subTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        submitContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submitContent.caseReference + caseNumber,
      ),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      ...Array.from({ length: 6 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 6}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 13}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage16}")`,
        ),
        4,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.chooseHearing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.changeReason}")`,
        ),
        1,
      ),
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
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage4}")`,
        ),
        1,
      );
    }
    if (venue !== null) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage5}")`,
        ),
        1,
      );
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.venueNull}")`,
        ),
        1,
      );
    }
    if (hearingAcrossMultipleDays) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage12}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `dl.complex-panel-title > dt > span.text-16:has-text("${submitContent.additionalHearingDateTitle}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${submitContent.additionalHearingDate}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${submitContent.textOnPage9}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${submitContent.additionalHearingDateTime}")`,
          ),
          3,
        ),
      ]);
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    hearing: string | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
  ): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-dynamic-list-field > span.text-16:text-is("${hearing}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingType}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingFormat}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${editListingListingDetailsContent.room}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${editListingListingDetailsContent.instructions}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${editListingRemoteHearingInformationContent.videoCallLink}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("${editListingRemoteHearingInformationContent.conferenceCallNumber}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-area-field > span:text-is("${editListingOtherInformationContent.otherInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-area-field > span:text-is("${editListingChangeReasonContent.reason}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span:text-is("${editListingNotifyPageContent.textOnPage3}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${editListingNotifyPageContent.textOnPage4}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${editListingNotifyPageContent.textOnPage5}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td > span.text-16:text-is("${editListingNotifyPageContent.textOnPage6}")`,
        ),
        1,
      ),
    ]);

    if (region) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-dynamic-list-field > span.text-16:text-is("${caseRegionCode}")`,
        ),
        1,
      );
    }
    if (venue !== null) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-dynamic-list-field > span.text-16:text-is("${venue}")`,
        ),
        1,
      );
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(`td > span.text-16:text-is("Venue not listed")`),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-text-field > span.text-16:text-is("Test Venue")`,
          ),
          1,
        ),
      ]);
    }
    if (!hearingAcrossMultipleDays) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(`ccd-read-yes-no-field > span.text-16:text-is("No")`),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
          ),
          1,
        ),
      ]);

      if (hearingSession === "Morning" || hearingSession === "All day") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-text-field > span.text-16:text-is("${editListingListingDetailsContent.morningTime}")`,
          ),
          1,
        );
      } else if (hearingSession === "Afternoon") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-text-field > span.text-16:text-is("${editListingListingDetailsContent.afternoonTime}")`,
          ),
          1,
        );
      }
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(`ccd-read-yes-no-field > span.text-16:text-is("Yes")`),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
          ),
          4,
        ),
      ]);

      if (hearingSession === "Morning" || hearingSession === "All day") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-text-field > span.text-16:text-is("${editListingListingDetailsContent.morningTime}")`,
          ),
          4,
        );
      } else if (hearingSession === "Afternoon") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-text-field > span.text-16:text-is("${editListingListingDetailsContent.afternoonTime}")`,
          ),
          4,
        );
      }
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
