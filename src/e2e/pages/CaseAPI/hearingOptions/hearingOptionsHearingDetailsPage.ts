import { expect, Page } from "@playwright/test";
import addCaseNotes_content from "../../../fixtures/content/CaseAPI/addNote/addCaseNotes_content.ts";
import hearingOptionsHearingDetailsContent from "../../../fixtures/content/CaseAPI/hearingOptions/hearingOptionsHearingDetails_content.ts";
import commonHelpers, {
  hearingFormat,
} from "../../../helpers/commonHelpers.ts";

type HearingOptionsHearingDetailsPage = {
  venue: string;
  venueNotListed: string;
  roomAtVenue: string;
  instructions: string;
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    venue: boolean,
    venueNotListed: boolean,
    hearingFormat: hearingFormat,
    shortNoticeHearing: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const hearingOptionsHearingDetails: HearingOptionsHearingDetailsPage = {
  venue: "#hearingVenues",
  venueNotListed: "#venueNotListedOption-VenueNotListed",
  roomAtVenue: "#roomAtVenue",
  instructions: "#addlInstr",
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${hearingOptionsHearingDetailsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p")).toContainText(
        addCaseNotes_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 11 }, (_, index) => {
        const textOnPage = (hearingOptionsHearingDetailsContent as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
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
      // await new AxeUtils(page).audit();
    }
  },

  async fillInFields(
    page: Page,
    venue: boolean,
    venueNotListed: boolean,
    hearingFormat: hearingFormat,
    shortNoticeHearing: boolean,
  ): Promise<void> {
    if (venue) {
      await page.selectOption(
        this.venue,
        hearingOptionsHearingDetailsContent.venue,
      );
    }
    if (venueNotListed) {
      await page.getByLabel("Venue not listed").check();
    }
    await page.fill(this.roomAtVenue, hearingOptionsHearingDetailsContent.room);
    await page.fill(
      this.instructions,
      hearingOptionsHearingDetailsContent.instructions,
    );
    await page.getByLabel(hearingFormat).check();
    if (shortNoticeHearing) {
      await page.getByLabel("Yes", { exact: true }).check();
    } else {
      await page.getByLabel("No", { exact: true }).check();
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default hearingOptionsHearingDetails;
