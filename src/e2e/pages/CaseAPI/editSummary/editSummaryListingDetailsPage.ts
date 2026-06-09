import { expect, Page } from "@playwright/test";
import editSummaryListingDetailsContent from "../../../fixtures/content/CaseAPI/editSummary/editSummaryListingDetails_content.ts";
import commonHelpers, {
  hearingSession,
  hearingVenues,
} from "../../../helpers/commonHelpers.ts";

type EditSummaryListingDetailsPage = {
  venue: string;
  venueNotListed: string;
  inputVenue: string;
  roomAtVenue: string;
  instructions: string;
  day: string;
  month: string;
  year: string;
  startTime: string;
  previous: string;
  continue: string;
  cancel: string;
  remove: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    venue: hearingVenues | null,
    subjectName: string,
  ): Promise<void>;
  checkFields(page: Page): Promise<void>;
  fillFields(
    page: Page,
    venue: hearingVenues | null,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editSummaryListingDetailsPage: EditSummaryListingDetailsPage = {
  venue: "#hearingVenues",
  venueNotListed: "#venueNotListedOption-VenueNotListed",
  inputVenue: "#hearingVenueNameAndAddress",
  roomAtVenue: "#roomAtVenue",
  instructions: "#addlInstr",
  day: "#date-day",
  month: "#date-month",
  year: "#date-year",
  startTime: "#hearingTime",
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  remove: "button[aria-label='Remove Additional Hearing date']",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    venue: hearingVenues | null,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editSummaryListingDetailsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editSummaryListingDetailsContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editSummaryListingDetailsContent.caseReference + caseNumber,
      ),
      expect(page.locator(".case-field__label").nth(2)).toHaveText(
        editSummaryListingDetailsContent.textOnPage1,
      ),
      expect(page.locator(".form-label").nth(2)).toHaveText(
        editSummaryListingDetailsContent.textOnPage2,
      ),
      expect(page.locator("markdown > h4")).toHaveText(
        editSummaryListingDetailsContent.subTitle1,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (editSummaryListingDetailsContent as any)[
          `textOnPage${index + 4}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (editSummaryListingDetailsContent as any)[
          `textOnPage${index + 13}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 7 }, (_, index) => {
        const textOnPage = (editSummaryListingDetailsContent as any)[
          `textOnPage${index + 6}`
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
    if (venue === null) {
      await expect(
        page.locator("label[for='hearingVenueNameAndAddress']"),
      ).toHaveText(`${editSummaryListingDetailsContent.textOnPage3}`);
    }
    // if (accessibilityTest) {
    //   await new AxeUtils(page).audit();
    // }
  },

  async checkFields(page: Page): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-text-field > span.text-16:text-is("East London Tribunal Hearing Centre")`,
        ),
        1,
      ),
      expect(page.locator(this.roomAtVenue)).toHaveValue(
        editSummaryListingDetailsContent.room,
      ),
      expect(page.locator(this.instructions)).toHaveValue(
        editSummaryListingDetailsContent.instructions,
      ),
      expect(page.locator(this.day)).toHaveValue(
        `${commonHelpers.padZero(currentDate.getDate())}`,
      ),
      expect(page.locator(this.month)).toHaveValue(
        `${commonHelpers.padZero(currentDate.getMonth() + 1)}`,
      ),
      expect(page.locator(this.year)).toHaveValue(
        `${currentDate.getFullYear()}`,
      ),
      expect(page.getByLabel("Morning").first()).toBeChecked(),
      expect(page.locator(this.startTime)).toHaveValue(
        editSummaryListingDetailsContent.morningTime,
      ),
      expect(page.getByLabel("No", { exact: true })).toBeChecked(),
    ]);
  },

  async fillFields(
    page: Page,
    venue: hearingVenues | null,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
  ): Promise<void> {
    const currentDate = new Date();
    if (venue === null) {
      await page
        .getByLabel(editSummaryListingDetailsContent.textOnPage2)
        .check();
      await page.fill(this.inputVenue, "Test Venue");
    }
    await page.fill(this.roomAtVenue, editSummaryListingDetailsContent.room);
    await page.fill(this.day, `${currentDate.getDate()}`);
    await page.fill(this.month, `${currentDate.getMonth() + 1}`);
    await page.fill(this.year, `${currentDate.getFullYear()}`);
    await page.getByLabel(hearingSession).nth(0).dispatchEvent("click");
    if (hearingSession === "Morning" || hearingSession === "All day") {
      await page.fill(
        this.startTime,
        editSummaryListingDetailsContent.morningTime,
      );
    } else if (hearingSession === "Afternoon") {
      await page.fill(
        this.startTime,
        editSummaryListingDetailsContent.afternoonTime,
      );
    }
    if (!hearingAcrossMultipleDays) {
      await page.getByLabel("No", { exact: true }).click();
    } else {
      await page.getByLabel("Yes", { exact: true }).click();
      await page.getByRole("button", { name: "Add new" }).click();
      await page.fill("#hearingVenueDate-day", `${currentDate.getDate()}`);
      await page.fill(
        "#hearingVenueDate-month",
        `${currentDate.getMonth() + 1}`,
      );
      await page.fill("#hearingVenueDate-year", `${currentDate.getFullYear()}`);
      await page.getByLabel(hearingSession).nth(1).dispatchEvent("click");
      if (hearingSession === "Morning" || hearingSession === "All day") {
        await page.fill(
          "#additionalHearingDate_0_hearingVenueTime",
          editSummaryListingDetailsContent.morningTime,
        );
      } else if (hearingSession === "Afternoon") {
        await page.fill(
          "#additionalHearingDate_0_hearingVenueTime",
          editSummaryListingDetailsContent.afternoonTime,
        );
      }
      await page.getByRole("button", { name: "Add new" }).nth(1).click();
      await page
        .locator("#hearingVenueDate-day")
        .nth(1)
        .fill(`${currentDate.getDate()}`);
      await page
        .locator("#hearingVenueDate-month")
        .nth(1)
        .fill(`${currentDate.getMonth() + 1}`);
      await page
        .locator("#hearingVenueDate-year")
        .nth(1)
        .fill(`${currentDate.getFullYear()}`);
      await page.getByLabel(hearingSession).nth(2).dispatchEvent("click");
      if (hearingSession === "Morning" || hearingSession === "All day") {
        await page
          .locator("#additionalHearingDate_1_hearingVenueTime")
          .fill(editSummaryListingDetailsContent.morningTime);
      } else if (hearingSession === "Afternoon") {
        await page
          .locator("#additionalHearingDate_1_hearingVenueTime")
          .fill(editSummaryListingDetailsContent.afternoonTime);
      }
      await page.getByRole("button", { name: "Add new" }).nth(1).click();
      await page
        .locator("#hearingVenueDate-day")
        .nth(2)
        .fill(`${currentDate.getDate()}`);
      await page
        .locator("#hearingVenueDate-month")
        .nth(2)
        .fill(`${currentDate.getMonth() + 1}`);
      await page
        .locator("#hearingVenueDate-year")
        .nth(2)
        .fill(`${currentDate.getFullYear()}`);
      await page.getByLabel(hearingSession).nth(3).dispatchEvent("click");
      if (hearingSession === "Morning" || hearingSession === "All day") {
        await page
          .locator("#additionalHearingDate_2_hearingVenueTime")
          .fill(editSummaryListingDetailsContent.morningTime);
      } else if (hearingSession === "Afternoon") {
        await page
          .locator("#additionalHearingDate_2_hearingVenueTime")
          .fill(editSummaryListingDetailsContent.afternoonTime);
      }
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.locator(this.day).clear();
    await page.locator(this.month).clear();
    await page.locator(this.year).clear();
    await page.locator(this.startTime).clear();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        editSummaryListingDetailsContent.errorBanner,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        editSummaryListingDetailsContent.hearingDateError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        editSummaryListingDetailsContent.timeError,
      ),
    ]);
    await page.getByLabel("Yes", { exact: true }).click();
    await page.getByRole("button", { name: "Add new" }).click();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        editSummaryListingDetailsContent.errorBanner,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        editSummaryListingDetailsContent.additionalHearingDateError,
      ),
      expect(page.locator(".error-message").nth(3)).toHaveText(
        editSummaryListingDetailsContent.sessionError,
      ),
      expect(page.locator(".error-message").nth(4)).toHaveText(
        editSummaryListingDetailsContent.additionalHearingTimeError,
      ),
    ]);
    await page.getByLabel("No", { exact: true }).click();
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default editSummaryListingDetailsPage;
