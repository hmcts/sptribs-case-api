import { expect, Page } from "@playwright/test";
import createListingListingDetailsContent from "../../../fixtures/content/CaseAPI/createListing/createListingListingDetails_content.ts";
import commonHelpers, {
  hearingSession,
  hearingVenues,
} from "../../../helpers/commonHelpers.ts";

type CreateListingListingDetailsPage = {
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
    errorMessaging: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    venue: hearingVenues | null,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createListingListingDetailsPage: CreateListingListingDetailsPage = {
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
    errorMessaging: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${createListingListingDetailsContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        createListingListingDetailsContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        createListingListingDetailsContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingListingDetailsContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index) => {
        const textOnPage = (createListingListingDetailsContent as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    await page
      .getByLabel(createListingListingDetailsContent.textOnPage2, {
        exact: true,
      })
      .check();
    await expect(
      page.locator("label[for='hearingVenueNameAndAddress']"),
    ).toHaveText(`${createListingListingDetailsContent.textOnPage3}`);
    await page
      .getByLabel(createListingListingDetailsContent.textOnPage2, {
        exact: true,
      })
      .click();
    await Promise.all([
      expect(page.locator("markdown > h4")).toHaveText(
        createListingListingDetailsContent.subTitle1,
      ),
      ...Array.from({ length: 14 }, (_, index) => {
        const textOnPage = (createListingListingDetailsContent as any)[
          `textOnPage${index + 4}`
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
    if (!errorMessaging) {
      await page.getByLabel("Yes", { exact: true }).click();
      await page.getByRole("button", { name: "Add new" }).click();
      await Promise.all([
        expect(page.locator(".heading-h2").nth(0)).toHaveText(
          createListingListingDetailsContent.subTitle2,
        ),
        // expect(
        //   page.locator("div.float-left > label > h3.heading-h3"),
        // ).toHaveText(createListingListingDetailsContent.subTitle2),
        // page.locator(this.remove).isVisible(),
        expect(
          page.locator("#hearingVenueDate > fieldset > legend > span"),
        ).toHaveText(createListingListingDetailsContent.subTitle1),
        ...Array.from({ length: 7 }, (_, index) => {
          const textOnPage = (createListingListingDetailsContent as any)[
            `textOnPage${index + 7}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.form-label:text-is("${textOnPage}")`),
            2,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.form-label:text-is("${createListingListingDetailsContent.textOnPage18}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.getByRole("button", { name: "Add new" }),
          2,
        ),
      ]);
      await page.click(this.remove);
      await expect(page.locator(".cdk-overlay-container")).toBeVisible();
      await page.locator("button[title='Remove']").click();
      await expect(page.locator(".cdk-overlay-container")).not.toBeVisible();
    }
    // if (accessibilityTest) {
    //   await new AxeUtils(page).audit();
    // }
  },

  async fillInFields(
    page: Page,
    venue: hearingVenues | null,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
  ): Promise<void> {
    if (venue !== null) {
      await page.selectOption(this.venue, venue);
    } else {
      await page
        .getByLabel(createListingListingDetailsContent.textOnPage2)
        .check();
      await page.fill(this.inputVenue, "Test Venue");
    }
    await page.fill(this.roomAtVenue, createListingListingDetailsContent.room);
    await page.fill(
      this.instructions,
      createListingListingDetailsContent.instructions,
    );
    const currentDate = new Date();
    await page.fill(this.day, `${currentDate.getDate()}`);
    await page.fill(this.month, `${currentDate.getMonth() + 1}`);
    await page.fill(this.year, `${currentDate.getFullYear()}`);
    await page.getByLabel(hearingSession).nth(0).dispatchEvent("click");
    if (hearingSession === "Morning" || hearingSession === "All day") {
      await page.fill(
        this.startTime,
        createListingListingDetailsContent.morningTime,
      );
    } else if (hearingSession === "Afternoon") {
      await page.fill(
        this.startTime,
        createListingListingDetailsContent.afternoonTime,
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
          createListingListingDetailsContent.morningTime,
        );
      } else if (hearingSession === "Afternoon") {
        await page.fill(
          "#additionalHearingDate_0_hearingVenueTime",
          createListingListingDetailsContent.afternoonTime,
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
          .fill(createListingListingDetailsContent.morningTime);
      } else if (hearingSession === "Afternoon") {
        await page
          .locator("#additionalHearingDate_1_hearingVenueTime")
          .fill(createListingListingDetailsContent.afternoonTime);
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
          .fill(createListingListingDetailsContent.morningTime);
      } else if (hearingSession === "Afternoon") {
        await page
          .locator("#additionalHearingDate_2_hearingVenueTime")
          .fill(createListingListingDetailsContent.afternoonTime);
      }
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".govuk-error-summary__title")).toHaveText(
        createListingListingDetailsContent.errorBanner,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        createListingListingDetailsContent.hearingDateError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        createListingListingDetailsContent.sessionError,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        createListingListingDetailsContent.timeError,
      ),
      expect(page.locator(".error-message").nth(3)).toHaveText(
        createListingListingDetailsContent.numberOfDaysError,
      ),
    ]);
    await page
      .getByLabel(createListingListingDetailsContent.textOnPage2)
      .check();
    await page.click(this.inputVenue);
    await page.click(this.roomAtVenue);
    await expect(page.locator(".error-message").nth(0)).toHaveText(
      createListingListingDetailsContent.hearingVenueError,
    );
    await page
      .getByLabel(createListingListingDetailsContent.textOnPage2)
      .click();
    await page.fill(this.day, "0");
    await page.click(this.month);
    await expect(page.locator(".error-message").nth(0)).toHaveText(
      createListingListingDetailsContent.invalidHearingDateError,
    );
    await page.locator(this.day).clear();
    await page.getByLabel("Yes", { exact: true }).click();
    await expect(page.locator(".error-message").nth(3)).toHaveText(
      createListingListingDetailsContent.additionalHearingDateError,
    );
    await page.getByLabel("No", { exact: true }).click();
    const currentDate = new Date();
    await page.fill(this.day, `${currentDate.getDate()}`);
    await page.fill(this.month, `${currentDate.getMonth() + 1}`);
    await page.fill(this.year, `${currentDate.getFullYear()}`);
    await page.getByLabel("Morning").dispatchEvent("click");
    await page.fill(
      this.startTime,
      createListingListingDetailsContent.morningTime,
    );
    await page.click(this.continue);
    await expect(page.locator(".error-summary-list")).toHaveText(
      createListingListingDetailsContent.validHearingVenueError,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default createListingListingDetailsPage;
