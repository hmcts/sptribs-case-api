import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import createSummaryHearingAttendeesRoleContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingAttendeesRole_content.ts";
import createSummaryHearingOutcomeContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingOutcome_content.ts";
import createSummaryHearingRecordingUploadContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingRecordingUpload_content.ts";
import createSummaryListingDetailsContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryListingDetails_content.ts";
import submitContent from "../../../fixtures/content/CaseAPI/createSummary/submit_content.ts";
import commonHelpers, {
  hearingAdjournedReasons,
  hearingFormat,
  hearingOutcome,
  hearingSession,
  hearingType,
  hearingVenueNames,
  hearingVenues,
} from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  hearingAttendees: string[];
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    hearingAcrossMultipleDays: boolean,
    fullPanelHearing: boolean,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    venue: hearingVenues | null,
    editJourney: boolean,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    panel: string[],
    fullPanelHearing: boolean,
    hearing: string | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    editJourney: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",
  hearingAttendees: [
    "Medical member",
    "Representative - non-legal",
    "Witness - Police",
    "Lay member",
    "Presenting Officer",
    "Tribunal Judge",
    "Main Appellant (on behalf of victim)",
    "Tribunal clerk",
    "Witness - General",
    "Other",
    "Counsel",
    "Observer",
    "Interpreter",
    "Victim",
    "Appraiser",
    "Representative - legal",
    "Appellant",
  ],

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    hearingAcrossMultipleDays: boolean,
    fullPanelHearing: boolean,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    venue: hearingVenues | null,
    editJourney: boolean,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForURL(
      `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/create-hearing-summary/submit`,
      { timeout: 30_000 },
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        submitContent.pageTitle,
      ),
      expect(page.locator("markdown > h3").first()).toContainText(
        `${subjectName}`,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submitContent.caseReference + caseNumber,
      ),
      expect(page.locator(".heading-h2")).toHaveText(submitContent.subTitle1),
      expect(page.locator("span.text-16").nth(0)).toHaveText(
        submitContent.textOnPage1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      ...Array.from({ length: 5 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 7}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      ...Array.from({ length: 5 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 12}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 19}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${textOnPage}")`,
          ),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `dl.complex-panel-title > dt > span.text-16:has-text("${submitContent.textOnPage19}")`,
        ),
        1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (submitContent as any)[`textOnPage${index + 21}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${textOnPage}")`,
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
    if (venue) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage5}")`,
        ),
        1,
      );
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage6}")`,
        ),
        1,
      );
    }
    if (hearingAcrossMultipleDays! && editJourney) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${submitContent.additionalHearingTitle}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `dl.complex-panel-title > dt > span.text-16:has-text("${submitContent.additionalHearingTitle}")`,
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
            `#complex-panel-simple-field-label > span.text-16:text-is("${submitContent.additionalHearingTime}")`,
          ),
          3,
        ),
      ]);
    }
    if (fullPanelHearing) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${submitContent.panelMemberTitle}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `dl.complex-panel-title > dt > span.text-16:has-text("${submitContent.panelMemberTitle}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${submitContent.panelMemberName}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${submitContent.panelMemberRole}")`,
          ),
          3,
        ),
      ]);
    }
    if (hearingOutcome === "Adjourned") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage17}")`,
        ),
        1,
      );
      if (hearingAdjournedReason === "Other") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${submitContent.textOnPage18}")`,
          ),
          1,
        );
      }
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    panel: string[],
    fullPanelHearing: boolean,
    hearing: string | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    editJourney: boolean,
  ): Promise<void> {
    const currentDate = new Date();
    switch (editJourney) {
      default:
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
              `ccd-read-text-field > span.text-16:text-is("${createSummaryListingDetailsContent.room}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-dynamic-list-field > span.text-16:text-is("${panel[0]}")`,
            ),
            1,
          ),
          ...Array.from(
            { length: this.hearingAttendees.length },
            (_, index) => {
              const attendee = (this.hearingAttendees as any)[index];
              return commonHelpers.checkVisibleAndPresent(
                page.locator(
                  `ccd-read-multi-select-list-field > table > tbody > tr > td > span:text-is("${attendee}")`,
                ),
                1,
              );
            },
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("${createSummaryHearingAttendeesRoleContent.otherAttendee}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingOutcome}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `table.complex-panel-table:has-text("A - Application Form")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `table.complex-panel-table:has-text("${createSummaryHearingRecordingUploadContent.description}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `table.complex-panel-table:has-text("${path.basename(config.testMP3File)}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-area-field > span:text-is("${createSummaryHearingRecordingUploadContent.recordingLocation}")`,
            ),
            1,
          ),
        ]);

        if (venue) {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("${venueName}")`,
            ),
            1,
          );
        } else {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(`td > span:text-is("Venue not listed")`),
            1,
          );
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("Test Venue")`,
            ),
            1,
          );
        }

        if (hearingOutcome === "Adjourned") {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingAdjournedReason}")`,
            ),
            1,
          );
          if (hearingAdjournedReason === "Other") {
            await commonHelpers.checkVisibleAndPresent(
              page.locator(
                `ccd-read-text-area-field > span:text-is("${createSummaryHearingOutcomeContent.otherAdjournedReason}")`,
              ),
              1,
            );
          }
        }

        if (!hearingAcrossMultipleDays) {
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `ccd-read-yes-no-field > span.text-16:text-is("No")`,
              ),
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
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
              ),
              1,
            );
          } else if (hearingSession === "Afternoon") {
            await commonHelpers.checkVisibleAndPresent(
              page.locator(
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
              ),
              1,
            );
          }
        } else {
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `ccd-read-yes-no-field > span.text-16:text-is("Yes")`,
              ),
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
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
              ),
              4,
            );
          } else if (hearingSession === "Afternoon") {
            await commonHelpers.checkVisibleAndPresent(
              page.locator(
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
              ),
              4,
            );
          }
        }

        if (!fullPanelHearing) {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("No. It was a 'sit alone' hearing")`,
            ),
            1,
          );
        } else {
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `ccd-read-fixed-radio-list-field > span.text-16:text-is("Yes")`,
              ),
              1,
            ),
            ...Array.from({ length: 3 }, (_, index) => {
              const member = panel[`${index + 1}`];
              return commonHelpers.checkVisibleAndPresent(
                page.locator(
                  `ccd-read-dynamic-list-field > span.text-16:text-is("${member}")`,
                ),
                1,
              );
            }),
            ...Array.from({ length: 3 }, (_, index) => {
              const role = (submitContent as any)[`role${index + 1}`];
              return commonHelpers.checkVisibleAndPresent(
                page.locator(
                  `ccd-read-fixed-radio-list-field > span.text-16:text-is("${role}")`,
                ),
                1,
              );
            }),
          ]);
        }
        break;
      case true:
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-dynamic-list-field > span.text-16:text-is("${hearing}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("Final")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("Video")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`td > span:text-is("Venue not listed")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("Edit Journey Test Venue")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("${createSummaryListingDetailsContent.room}")`,
            ),
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
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("Afternoon")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-dynamic-list-field > span.text-16:text-is("${panel[0]}")`,
            ),
            1,
          ),
          ...Array.from(
            { length: this.hearingAttendees.length },
            (_, index) => {
              const attendee = (this.hearingAttendees as any)[index];
              return commonHelpers.checkVisibleAndPresent(
                page.locator(
                  `ccd-read-multi-select-list-field > table > tbody > tr > td > span:text-is("${attendee}")`,
                ),
                1,
              );
            },
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-field > span.text-16:text-is("${createSummaryHearingAttendeesRoleContent.otherAttendee}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingOutcome}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `table.complex-panel-table:has-text("A - Application Form")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `table.complex-panel-table:has-text("${createSummaryHearingRecordingUploadContent.description}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `table.complex-panel-table:has-text("${path.basename(config.testMP3File)}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-read-text-area-field > span:text-is("${createSummaryHearingRecordingUploadContent.recordingLocation}")`,
            ),
            1,
          ),
        ]);
        break;
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.saveAndContinue);
  },
};

export default submitPage;
