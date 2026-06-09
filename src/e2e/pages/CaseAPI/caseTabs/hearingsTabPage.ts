import { AxeUtils } from "@hmcts/playwright-common";
import { Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import cancelHearingReason_content from "../../../fixtures/content/CaseAPI/cancelHearing/cancelHearingReason_content.ts";
import hearingsTab_content from "../../../fixtures/content/CaseAPI/caseTabs/hearingsTab_content.ts";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import createListingOtherInformationContent from "../../../fixtures/content/CaseAPI/createListing/createListingOtherInformation_content.ts";
import createListingRemoteHearingInformationContent from "../../../fixtures/content/CaseAPI/createListing/createListingRemoteHearingInformation_content.ts";
import createSummaryHearingAttendeesRoleContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingAttendeesRole_content.ts";
import createSummaryHearingRecordingUploadContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingRecordingUpload_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/createSummary/submit_content.ts";
import editListingChangeReasonContent from "../../../fixtures/content/CaseAPI/editListing/editListingChangeReason_content.ts";
import casePanelComposition_content from "../../../fixtures/content/CaseAPI/panelComposition/casePanelComposition_content.ts";
import postponeHearingReason_content from "../../../fixtures/content/CaseAPI/postponeHearing/postponeHearingReason_content.ts";
import commonHelpers, {
  caseRegionCode,
  hearingAdjournedReasons,
  hearingCancelledReasons,
  hearingFormat,
  hearingOutcome,
  hearingPostponedReasons,
  hearingSession,
  hearingType,
  hearingVenueNames,
  hearingVenues,
} from "../../../helpers/commonHelpers.ts";
import {
  Panel2,
  Panel3,
} from "../panelComposition/casePanelCompositionPage.ts";

type HearingsTabPage = {
  hearingsTab: string;
  listingTable: string;
  hearingAttendees: string[];
  checkPageLoads(
    page: Page,
    region: boolean,
    hearingAcrossMultipleDays: boolean,
    readyToList: boolean,
    venue: hearingVenues | null,
    createSummary: boolean,
    editSummary: boolean,
    hearingOutcome: hearingOutcome | null,
    fullPanelHearing: boolean,
    editJourney: boolean,
    cancelHearing: boolean,
    postponeHearing: boolean,
    editListing: boolean,
    accessibilityTest: boolean,
  ): Promise<void>;
  changeToHearingsTab(page: Page): Promise<void>;
  checkPanelComposition(
    page: Page,
    panel2: Panel2,
    panel3: Panel3,
    specialisms: boolean,
  ): Promise<void>;
  checkValidInfoCreateListing(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    readyToList: boolean,
    venue: hearingVenues | null,
    editListing: boolean,
  ): Promise<void>;
  checkValidInfoCreateSummary(
    page: Page,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    panel: string[],
    fullPanelHearing: boolean,
    editJourney: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfoCancelHearing(
    page: Page,
    reasonCancelled: hearingCancelledReasons,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    venue: hearingVenues | null,
  ): Promise<void>;
  checkValidInfoPostponeHearing(
    page: Page,
    reasonPostponed: hearingPostponedReasons,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    venue: hearingVenues | null,
  ): Promise<void>;
};

const hearingTabPage: HearingsTabPage = {
  hearingsTab: `.mat-tab-label-content:text-is("Hearings")`,
  listingTable:
    "table > tbody > tr > td > span > ccd-field-read > div > ccd-field-read-label > div > ",
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
    region: boolean,
    hearingAcrossMultipleDays: boolean,
    readyToList: boolean,
    venue: hearingVenues | null,
    createSummary: boolean,
    editSummary: boolean,
    hearingOutcome: hearingOutcome | null,
    fullPanelHearing: boolean,
    editJourney: boolean,
    cancelHearing: boolean,
    postponeHearing: boolean,
    editListing: boolean,
    accessibilityTest: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `div.case-viewer-label:text-is("${hearingsTab_content.title}")`,
    );
    await page.waitForSelector(
      `span.text-16:text-is("${hearingsTab_content.subtitle2}")`,
    );
    await Promise.all([
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (hearingsTab_content as any)[
          `textOnPage${index + 5}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage12}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage14}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${hearingsTab_content.textOnPage15}")`),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage16}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage17}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage19}")`,
        ),
        1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (hearingsTab_content as any)[
          `textOnPage${index + 21}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h4:text-is("${hearingsTab_content.subtitle3}")`,
        ),
        1,
      ),
    ]);
    if (cancelHearing) {
      await Promise.all([
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 35}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
      ]);
    }
    if (postponeHearing) {
      await Promise.all([
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 38}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
      ]);
    }
    if (editListing) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.changeReason}")`,
        ),
        1,
      );
    }
    if (region) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage11}")`,
        ),
        1,
      );
    }
    if (hearingAcrossMultipleDays) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `span.text-16:text-is("${hearingsTab_content.textOnPage20}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `dl.complex-panel-title > dt > span.text-16:has-text("${hearingsTab_content.additionalHearingDateTitle}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${hearingsTab_content.additionalHearingDate}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${hearingsTab_content.additionalHearingDateTime}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#complex-panel-simple-field-label > span.text-16:text-is("${hearingsTab_content.textOnPage18}")`,
          ),
          4,
        ),
      ]);
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#complex-panel-simple-field-label > span.text-16:text-is("${hearingsTab_content.textOnPage18}")`,
        ),
        1,
      );
    }
    if (readyToList) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.shortHearing}")`,
        ),
        1,
      );
    }
    if (venue) {
      if (editJourney) {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${hearingsTab_content.textOnPage10}")`,
          ),
          1,
        );
      } else {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${hearingsTab_content.textOnPage10}")`,
            ),
            2,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `span.text-16:text-is("${hearingsTab_content.textOnPage13}")`,
            ),
            1,
          ),
        ]);
      }
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span.text-16:text-is("${hearingsTab_content.textOnPage9}")`,
        ),
        1,
      );
    }

    if (createSummary) {
      await Promise.all([
        ...Array.from({ length: 2 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 24}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 26}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 29}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > dl > dt > span:has-text("${hearingsTab_content.textOnPage31}")`,
          ),
          1,
        ),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 32}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div> ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > th > span:has-text("${textOnPage}")`,
            ),
            1,
          );
        }),
      ]);
      if (hearingOutcome == "Adjourned") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `span.text-16:text-is("${hearingsTab_content.hearingAdjournedReason}")`,
          ),
          1,
        );
      }
      if (fullPanelHearing) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `span.text-16:text-is("${hearingsTab_content.panelMemberTitle}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `dl.complex-panel-title > dt > span.text-16:has-text("${hearingsTab_content.panelMemberTitle}")`,
            ),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#complex-panel-simple-field-label > span.text-16:text-is("${hearingsTab_content.panelMemberName}")`,
            ),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#complex-panel-simple-field-label > span.text-16:text-is("${hearingsTab_content.panelMemberRole}")`,
            ),
            3,
          ),
        ]);
      }
    }

    if (editSummary) {
      await Promise.all([
        ...Array.from({ length: 2 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 24}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 26}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 29}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`span.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > dl > dt > span:has-text("${hearingsTab_content.textOnPage31}")`,
          ),
          1,
        ),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (hearingsTab_content as any)[
            `textOnPage${index + 32}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div> ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > th > span:has-text("${textOnPage}")`,
            ),
            1,
          );
        }),
      ]);
      if (hearingOutcome == "Adjourned") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `span.text-16:text-is("${hearingsTab_content.hearingAdjournedReason}")`,
          ),
          1,
        );
      }
      if (fullPanelHearing) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `span.text-16:text-is("${hearingsTab_content.panelMemberTitle}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > dl > dt > span:has-text("${hearingsTab_content.panelMemberTitle}")`,
            ),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > th > span:text-is("${hearingsTab_content.panelMemberName}")`,
            ),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > th > span:text-is("${hearingsTab_content.panelMemberRole2}")`,
            ),
            3,
          ),
        ]);
      }
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async changeToHearingsTab(page: Page): Promise<void> {
    await page.waitForSelector(this.hearingsTab);
    // For tab clicking flakiness
    await page.locator(this.hearingsTab).click();
    await page.locator(this.hearingsTab).click();
    await page.locator(this.hearingsTab).click();
  },

  async checkPanelComposition(
    page: Page,
    panel2: Panel2,
    panel3: Panel3,
    specialisms: boolean,
  ): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`h4:text-is("${hearingsTab_content.subtitle1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${hearingsTab_content.textOnPage1}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Tribunal Judge")`),
        1,
      ),
    ]);
    if (panel2 !== null) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${hearingsTab_content.textOnPage2}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${panel2}")`),
          1,
        ),
      ]);
    }
    if (panel3 !== null) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${hearingsTab_content.textOnPage3}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${panel3}")`),
          1,
        ),
      ]);
    }
    if (specialisms) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${hearingsTab_content.textOnPage4}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `span:text-is("Lorem ipsum ${casePanelComposition_content.textOnPage2}")`,
          ),
          1,
        ),
      ]);
    }
  },

  async checkValidInfoCreateListing(
    page: Page,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    readyToList: boolean,
    venue: hearingVenues | null,
    editListing: boolean,
  ): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingsTab_content.listedStatus}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingType}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingFormat}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.room}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${createListingListingDetailsContent.instructions}")`,
        ),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.videoCallLink}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.conferenceCallNumber}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${createListingOtherInformationContent.otherInformation}")`,
        ),
        1,
      ),
    ]);
    if (region) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("${caseRegionCode}")`),
        1,
      );
    }
    if (!hearingAcrossMultipleDays) {
      if (readyToList) {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("No")`),
          3,
        );
      } else {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("No")`),
          1,
        );
      }
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
          ),
          2,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
          ),
          1,
        ),
      ]);
      if (hearingSession === "Morning" || hearingSession === "All day") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
          ),
          1,
        );
      } else if (hearingSession === "Afternoon") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
          ),
          1,
        );
      }
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
          ),
          5,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("Yes")`).first(),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
          ),
          4,
        ),
      ]);
      if (hearingSession === "Morning" || hearingSession === "All day") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
          ),
          4,
        );
      } else if (hearingSession === "Afternoon") {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
          ),
          4,
        );
      }
    }
    if (venue !== null) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-dynamic-list-field > span.text-16:text-is("${venue}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("${venue}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `td[id='case-viewer-field-read--hearingVenues'] span[class='text-16'] span[class='text-16']:text-is("${venue}")`,
          ),
          1,
        ),
      ]);
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("Venue not listed")`),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("Test Venue")`,
          ),
          1,
        ),
      ]);
    }
    if (editListing) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${editListingChangeReasonContent.reason}")`,
        ),
        1,
      );
    }
  },

  async checkValidInfoCreateSummary(
    page: Page,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    panel: string[],
    fullPanelHearing: boolean,
    editJourney: boolean,
    subjectName: string,
  ): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingsTab_content.completedStatus}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.room}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${createListingListingDetailsContent.instructions}")`,
        ),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("1-London")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.videoCallLink}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.conferenceCallNumber}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${createListingOtherInformationContent.otherInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-dynamic-list-field > span:text-is("${panel[0]}")`,
        ),
        1,
      ),
      ...Array.from({ length: this.hearingAttendees.length }, (_, index) => {
        const attendee = (this.hearingAttendees as any)[index];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-multi-select-list-field > table > tbody > tr > td > span:text-is("${attendee}")`,
          ),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createSummaryHearingAttendeesRoleContent.otherAttendee}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingOutcome}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${subjectName}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${createSummaryHearingRecordingUploadContent.recordingLocation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > td > span > ccd-field-read > div > ccd-field-read-label > div > ccd-read-fixed-list-field > span:has-text("A - Application Form")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > td > span > ccd-field-read > div > ccd-field-read-label > div > ccd-read-text-area-field > span:has-text("${createSummaryHearingRecordingUploadContent.description}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > td > span > ccd-field-read > div > ccd-field-read-label > div > ccd-read-document-field > button:has-text("${path.basename(config.testMP3File)}")`,
        ),
        1,
      ),
    ]);
    if (hearingOutcome === "Adjourned") {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingAdjournedReason}")`,
        ),
        1,
      );
    }
    if (editJourney) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span:text-is("Final")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("Video")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-multi-select-list-field > table > tbody > tr > td > span:text-is("Venue not listed")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("Edit Journey Test Venue")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("14:00")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("Afternoon")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`ccd-read-yes-no-field > span.text-16:text-is("No")`),
          1,
        ),
      ]);
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span:text-is("${hearingType}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingFormat}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `td[id='case-viewer-field-read--hearingVenues'] span[class='text-16'] span[class='text-16']:text-is("${venue}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-dynamic-list-field > span.text-16:text-is("${venue}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-text-field > span.text-16:text-is("${venueName}")`,
          ),
          2,
        ),
      ]);
      if (!hearingAcrossMultipleDays) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(`ccd-read-yes-no-field > span.text-16:text-is("No")`),
            1,
          ),

          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
            ),
            2,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
            ),
            1,
          ),
        ]);
        if (hearingSession === "Morning" || hearingSession === "All day") {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
            ),
            1,
          );
        } else if (hearingSession === "Afternoon") {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
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
              this.listingTable +
                `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
            ),
            5,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
            ),
            4,
          ),
        ]);
        if (hearingSession === "Morning" || hearingSession === "All day") {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
            ),
            4,
          );
        } else if (hearingSession === "Afternoon") {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.afternoonTime}")`,
            ),
            4,
          );
        }
      }
      if (!fullPanelHearing) {
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            this.listingTable +
              `ccd-read-fixed-radio-list-field > span.text-16:text-is("No. It was a 'sit alone' hearing")`,
          ),
          1,
        );
      } else {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                `ccd-read-fixed-radio-list-field > span.text-16:text-is("Yes")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              this.listingTable +
                ` ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > dl > dt > span:has-text("${hearingsTab_content.panelMemberTitle}")`,
            ),
            3,
          ),
          ...Array.from({ length: 3 }, (_, index) => {
            const member = panel[`${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(
                this.listingTable +
                  `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > td > span > ccd-field-read > div > ccd-field-read-label > div > ccd-read-dynamic-list-field > span:text-is("${member}")`,
              ),
              1,
            );
          }),
          ...Array.from({ length: 3 }, (_, index) => {
            const role = (submit_content as any)[`role${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(
                this.listingTable +
                  `ccd-read-collection-field > table > tbody > tr > td > ccd-field-read > div > ccd-field-read-label > div > ccd-read-complex-field > ccd-read-complex-field-table > div > table > tbody > tr > td > span > ccd-field-read > div > ccd-field-read-label > div > ccd-read-fixed-radio-list-field > span:text-is("${role}")`,
              ),
              1,
            );
          }),
        ]);
      }
    }
  },

  async checkValidInfoCancelHearing(
    page: Page,
    reasonCancelled: hearingCancelledReasons,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    venue: hearingVenues | null,
  ): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingsTab_content.cancelledStatus}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${reasonCancelled}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${cancelHearingReason_content.otherImportantInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingType}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingFormat}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.room}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${createListingListingDetailsContent.instructions}")`,
        ),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.videoCallLink}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.conferenceCallNumber}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${createListingOtherInformationContent.otherInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-dynamic-list-field > span.text-16:text-is("${venue}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${venue}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td[id='case-viewer-field-read--hearingVenues'] span[class='text-16'] span[class='text-16']:text-is("${venue}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("${caseRegionCode}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("No")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
        ),
        3,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
        ),
        1,
      ),
    ]);
  },

  async checkValidInfoPostponeHearing(
    page: Page,
    reasonPostponed: hearingPostponedReasons,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    venue: hearingVenues | null,
  ): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingsTab_content.postponedStatus}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${reasonPostponed}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${postponeHearingReason_content.otherImportantInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span:text-is("${hearingType}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingFormat}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.room}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${createListingListingDetailsContent.instructions}")`,
        ),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.videoCallLink}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingRemoteHearingInformationContent.conferenceCallNumber}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-area-field > span:text-is("${createListingOtherInformationContent.otherInformation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-dynamic-list-field > span.text-16:text-is("${venue}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${venue}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `td[id='case-viewer-field-read--hearingVenues'] span[class='text-16'] span[class='text-16']:text-is("${venue}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("${caseRegionCode}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("No")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
        ),
        3,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-fixed-radio-list-field > span.text-16:text-is("${hearingSession}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          this.listingTable +
            `ccd-read-text-field > span.text-16:text-is("${createListingListingDetailsContent.morningTime}")`,
        ),
        1,
      ),
    ]);
  },
};

export default hearingTabPage;
