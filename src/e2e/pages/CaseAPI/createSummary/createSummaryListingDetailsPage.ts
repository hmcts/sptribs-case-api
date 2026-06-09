import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createSummaryListingDetailsContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryListingDetails_content.ts";
import commonHelpers, {
  hearingSession,
  hearingVenueNames,
  hearingVenues,
} from "../../../helpers/commonHelpers.ts";

type CreateSummaryListingDetailsPage = {
  venueNotListed: string;
  inputVenue: string;
  roomAtVenue: string;
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
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    subjectName: string,
  ): Promise<void>;
  checkFields(
    page: Page,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    editJourney: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createSummaryListingDetailsPage: CreateSummaryListingDetailsPage = {
  venueNotListed: "#venueNotListedOption-VenueNotListed",
  inputVenue: "#hearingVenueNameAndAddress",
  roomAtVenue: "#roomAtVenue",
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
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${createSummaryListingDetailsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        createSummaryListingDetailsContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        createSummaryListingDetailsContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createSummaryListingDetailsContent.caseReference + caseNumber,
      ),
      expect(page.locator(".case-field__label").nth(2)).toHaveText(
        createSummaryListingDetailsContent.textOnPage1,
      ),
      expect(page.locator(".form-label").nth(2)).toHaveText(
        createSummaryListingDetailsContent.textOnPage2,
      ),
      expect(page.locator("markdown > h4")).toHaveText(
        createSummaryListingDetailsContent.subTitle1,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (createSummaryListingDetailsContent as any)[
          `textOnPage${index + 4}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (createSummaryListingDetailsContent as any)[
          `textOnPage${index + 13}`
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
      ).toHaveText(`${createSummaryListingDetailsContent.textOnPage3}`);
    }

    if (!hearingAcrossMultipleDays) {
      await Promise.all([
        ...Array.from({ length: 7 }, (_, index) => {
          const textOnPage = (createSummaryListingDetailsContent as any)[
            `textOnPage${index + 6}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.form-label:text-is("${textOnPage}")`),
            1,
          );
        }),
      ]);
    } else {
      await Promise.all([
        expect(page.locator(".heading-h2").nth(0)).toHaveText(
          createSummaryListingDetailsContent.subTitle2,
        ),
        ...Array.from({ length: 7 }, (_, index) => {
          const textOnPage = (createSummaryListingDetailsContent as any)[
            `textOnPage${index + 6}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.form-label:text-is("${textOnPage}")`),
            4,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `div.float-left > label > h3.heading-h3:has-text("${createSummaryListingDetailsContent.additionalHearingDateTitle}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.form-label:text-is("${createSummaryListingDetailsContent.additionalHearingDate}")`,
          ),
          3,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.form-label:text-is("${createSummaryListingDetailsContent.additionalHearingDateTime}")`,
          ),
          3,
        ),
      ]);
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkFields(
    page: Page,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    editJourney: boolean,
  ): Promise<void> {
    const currentDate = new Date();
    if (venue) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${venueName}")`),
        1,
      );
    } else {
      await expect(
        page.locator("#venueNotListedOption-VenueNotListed"),
      ).toBeChecked();
    }
    await Promise.all([
      expect(page.locator(this.roomAtVenue)).toHaveValue(
        createSummaryListingDetailsContent.room,
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
      expect(page.getByLabel(hearingSession).first()).toBeChecked(),
    ]);
    if (hearingSession === "Morning" || hearingSession === "All day") {
      await expect(page.locator(this.startTime)).toHaveValue(
        createSummaryListingDetailsContent.morningTime,
      );
    } else if (hearingSession === "Afternoon") {
      await expect(page.locator(this.startTime)).toHaveValue(
        createSummaryListingDetailsContent.afternoonTime,
      );
    }
    if (!hearingAcrossMultipleDays) {
      await expect(page.getByLabel("No", { exact: true })).toBeChecked();
    } else {
      await expect(page.getByLabel("Yes", { exact: true })).toBeChecked();
      if (hearingSession === "Morning") {
        for (let i = 0; i < 3; i++) {
          await expect(
            page.locator(`#additionalHearingDate_${i}_hearingVenueTime`),
          ).toHaveValue(createSummaryListingDetailsContent.morningTime);
          await expect(
            page.locator(
              `#additionalHearingDate_${i}_hearingVenueSession-morning`,
            ),
          ).toBeChecked();
        }
      } else if (hearingSession === "All day") {
        for (let i = 0; i < 3; i++) {
          await expect(
            page.locator(`#additionalHearingDate_${i}_hearingVenueTime`),
          ).toHaveValue(createSummaryListingDetailsContent.morningTime);
          await expect(
            page.locator(
              `#additionalHearingDate_${i}_hearingVenueSession-allDay`,
            ),
          ).toBeChecked();
        }
      } else if (hearingSession === "Afternoon") {
        for (let i = 0; i < 3; i++) {
          await expect(
            page.locator(`#additionalHearingDate_${i}_hearingVenueTime`),
          ).toHaveValue(createSummaryListingDetailsContent.afternoonTime);
          await expect(
            page.locator(
              `#additionalHearingDate_${i}_hearingVenueSession-afternoon`,
            ),
          ).toBeChecked();
        }
      }
      for (let i = 0; i < 3; i++) {
        await expect(page.locator("#hearingVenueDate-day").nth(i)).toHaveValue(
          `${commonHelpers.padZero(currentDate.getDate())}`,
        );
        await expect(
          page.locator("#hearingVenueDate-month").nth(i),
        ).toHaveValue(`${commonHelpers.padZero(currentDate.getMonth() + 1)}`);
        await expect(page.locator("#hearingVenueDate-year").nth(i)).toHaveValue(
          `${currentDate.getFullYear()}`,
        );
      }
    }
    if (editJourney) {
      await page.fill(this.inputVenue, "Edit Journey Test Venue");
      await page.getByLabel("No", { exact: true }).click();
      await page.getByLabel("Afternoon").dispatchEvent("click");
      await page.fill(
        this.startTime,
        createSummaryListingDetailsContent.afternoonTime,
      );
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.locator(this.inputVenue).clear();
    await page.locator(this.day).clear();
    await page.locator(this.month).clear();
    await page.locator(this.year).clear();
    await page.locator(this.startTime).clear();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        createSummaryListingDetailsContent.errorBanner,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        createSummaryListingDetailsContent.hearingVenueError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        createSummaryListingDetailsContent.hearingDateError,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        createSummaryListingDetailsContent.timeError,
      ),
    ]);
    await page.getByLabel("Yes", { exact: true }).click();
    await page.getByRole("button", { name: "Add new" }).click();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        createSummaryListingDetailsContent.errorBanner,
      ),
      expect(page.locator(".error-message").nth(3)).toHaveText(
        createSummaryListingDetailsContent.additionalHearingDateError,
      ),
      expect(page.locator(".error-message").nth(4)).toHaveText(
        createSummaryListingDetailsContent.sessionError,
      ),
      expect(page.locator(".error-message").nth(5)).toHaveText(
        createSummaryListingDetailsContent.additionalHearingTimeError,
      ),
    ]);
    await page.getByLabel("No", { exact: true }).click();
    const currentDate = new Date();
    await page.fill(this.inputVenue, "Test Venue");
    await page.fill(this.day, `${currentDate.getDate()}`);
    await page.fill(this.month, `${currentDate.getMonth() + 1}`);
    await page.fill(this.year, `${currentDate.getFullYear()}`);
    await page.fill(
      this.startTime,
      createSummaryListingDetailsContent.morningTime,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default createSummaryListingDetailsPage;
