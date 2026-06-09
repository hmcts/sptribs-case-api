import { expect, Locator, Page } from "@playwright/test";
import axios from "axios";
import { randomBytes } from "crypto";
import * as fs from "node:fs";
import { UserRole } from "../config.ts";
import authors_content from "../fixtures/content/authors_content.ts";
import allTabTitles_content from "../fixtures/content/CaseAPI/caseTabs/allTabTitles_content.ts";
import caseDocumentsUploadObject_content from "../fixtures/content/CaseAPI/createCase/caseDocumentsUploadObject_content.ts";
import createSummaryListingDetails_content from "../fixtures/content/CaseAPI/createSummary/createSummaryListingDetails_content.ts";
import uploadCaseDocuments_content from "../fixtures/content/CaseAPI/documentManagementUpload/uploadCaseDocuments_content.ts";
import blank from "../fixtures/content/CaseAPI/documents/blank.ts";
import eligibility from "../fixtures/content/CaseAPI/documents/eligibility.ts";
import generalDirections from "../fixtures/content/CaseAPI/documents/generalDirections.ts";
import loGeneralDirections from "../fixtures/content/CaseAPI/documents/loGeneralDirections.ts";
import MeDmi from "../fixtures/content/CaseAPI/documents/MeDmi.ts";
import MeJoint from "../fixtures/content/CaseAPI/documents/MeJoint.ts";
import proFormaSummons from "../fixtures/content/CaseAPI/documents/proFormaSummons.ts";
import quantum from "../fixtures/content/CaseAPI/documents/quantum.ts";
import rule27 from "../fixtures/content/CaseAPI/documents/rule27.ts";
import strikeoutNotice from "../fixtures/content/CaseAPI/documents/strikeoutNotice.ts";
import strikeoutWarning from "../fixtures/content/CaseAPI/documents/strikeoutWarning.ts";
import editDraftAddDocumentFooter_content from "../fixtures/content/CaseAPI/editDraft/editDraftAddDocumentFooter_content.ts";
import editDraftOrderMainContent_content from "../fixtures/content/CaseAPI/editDraft/editDraftOrderMainContent_content.ts";
import addDocumentFooter_content from "../fixtures/content/CaseAPI/issueFinalDecision/addDocumentFooter_content.ts";
import finalDecisionMain_content from "../fixtures/content/CaseAPI/issueFinalDecision/finalDecisionMain_content.ts";
import CookiesContent from "../fixtures/content/cookies_content.ts";
import subjectDetailsPage from "../fixtures/content/DSSCreateCase/SubjectDetails_content.ts";
import CaseFinderContent from "../fixtures/content/DSSUpdateCase/CaseFinder_content.ts";
import feedbackBanner_content from "../fixtures/content/DSSUpdateCase/feedbackBanner_content.ts";
import { Template } from "../pages/CaseAPI/issueFinalDecision/selectTemplatePage.ts";
import idamLoginHelper from "./idamLoginHelper.ts";

interface CommonHelpers {
  readonly months: string[];
  shortMonths(index: number): Promise<string>;
  todayDate(): Promise<string>;
  todayDateDoc(): Promise<string>;
  futureDate(numberOfdays: number): Promise<string>;
  todayDateFull(): Promise<string>;
  padZero(value: number): string;
  postcodeHandler(page: Page, party: string): Promise<void>;
  convertDate(tab: boolean): Promise<string>;
  getTimestamp(): Promise<string>;
  uploadFileController(
    page: Page,
    selector: string,
    docNumber: number,
    documentCategory: documentCategory,
    file: string,
    docManagementUpload: boolean,
  ): Promise<void>;
  checkVisibleAndPresent(locator: Locator, count: number): Promise<void>;
  checkAndAcceptCookies(
    page: Page,
    cy: boolean,
    service: string,
  ): Promise<void>;
  chooseEventFromDropdown(page: Page, chosenEvent: allEvents): Promise<any>;
  checkNumberAndSubject(
    page: Page,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  checkAllCaseTabs(
    page: Page,
    caseNumber: string,
    respondent: boolean,
    subjectName: string,
  ): Promise<void>;
  generateUrl(baseURL: string, caseNumber: string): Promise<string>;
  feedbackBanner(page: Page, cy: boolean, landingPage: boolean): Promise<void>;
  signOutAndGoToCase(
    page: Page,
    user: UserRole,
    baseURL: string,
    caseNumber: string,
  ): Promise<void>;
  checkForButtons(
    page: Page,
    continueButton: string,
    previous: string,
    cancel: string,
  ): Promise<void>;
  checkCommonDocument(
    newPage: Page,
    caseNumber: string,
    caseNoticeType: CaseNoticeType,
    template: Template,
    editDraftJourney: boolean,
    subjectName: string,
  ): Promise<void>;
  checkDocument(
    page: Page,
    template: Template,
    caseNumber: string,
    noticeType: CaseNoticeType,
    editDraftJourney: boolean,
    subjectName: string,
  ): Promise<void>;
  randomLetters(length: number): string;
}

const commonHelpers: CommonHelpers = {
  months: [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ],

  randomLetters(length: number): string {
    return Array.from({ length }, () =>
      String.fromCharCode(65 + (randomBytes(1)[0] % 26)),
    ).join("");
  },

  async shortMonths(index: number): Promise<string> {
    const monthFullName = this.months[index - 1];
    return monthFullName.substring(0, 3);
  },

  async todayDate(): Promise<string> {
    const now = new Date();
    const dateString = now.toLocaleDateString("en-US", {
      year: "numeric",
      month: "2-digit",
      day: "numeric",
    });
    const [month, day, year] = dateString.split("/");

    return `${day} ${await commonHelpers.shortMonths(parseInt(month))} ${year}`;
  },

  async todayDateDoc(): Promise<string> {
    const now = new Date();
    const dateString = now.toLocaleDateString("en-US", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
    const [month, day, year] = dateString
      .split("/")
      .map((part) => part.padStart(2, "0"));

    return `${day} ${await commonHelpers.shortMonths(parseInt(month))} ${year}`;
  },

  async futureDate(numberOfDays: number): Promise<string> {
    const privilegeDayPath = "src/tests/fixtures/privilegeDay.json";

    const fetchBankHolidays = async () => {
      try {
        const response = await axios.get(
          "https://www.gov.uk/bank-holidays/scotland.json",
        );
        return response.data;
      } catch (error) {
        console.error("Failed to fetch bank holidays:", error);
        throw new Error("Could not fetch bank holidays");
      }
    };

    const fetchPrivilegeDay = async () => {
      const data = fs.readFileSync(privilegeDayPath, "utf-8");
      const privilegeDayData = JSON.parse(data);
      return privilegeDayData.events.map((event: any) => event.date);
    };

    const today = new Date();
    let workingDaysCount = 0;

    const holidaysData = await fetchBankHolidays();
    const holidays: string[] = holidaysData.events
      .filter((event: any) => {
        const holidayDate = new Date(event.date);
        const dayOfWeek = holidayDate.getDay();
        return dayOfWeek !== 0 && dayOfWeek !== 6;
      })
      .map((event: any) => event.date);

    const privilegeDay: string[] = await fetchPrivilegeDay();
    const allHolidays = new Set([...holidays, ...privilegeDay]);

    while (workingDaysCount < numberOfDays) {
      today.setDate(today.getDate() + 1);

      const dayOfWeek = today.getDay();
      const formattedDate = today.toISOString().split("T")[0];

      if (
        dayOfWeek !== 0 &&
        dayOfWeek !== 6 &&
        !allHolidays.has(formattedDate)
      ) {
        workingDaysCount++;
      }
    }
    const day = today.getDate();
    const month = today.toLocaleString("en-US", { month: "long" });
    const year = today.getFullYear();

    return `${day} ${month} ${year}`;
  },

  async todayDateFull(): Promise<string> {
    const now = new Date();

    const day = now.getDate();
    const month = now.toLocaleString("en-US", { month: "long" });
    const year = now.getFullYear();

    return `${day} ${month} ${year}`;
  },

  padZero(value: number): string {
    return value < 10 ? "0" + value : value.toString();
  },

  async postcodeHandler(page: Page, party: string): Promise<void> {
    let findAddress: string;
    let postCode: string;
    let selectAddress: string;
    let buildingAndStreet: string;
    let addressLine2: string;
    let addressLine3: string;
    let townOrCity: string;
    let countyState: string;
    let country: string;
    let postcodeZipcode: string;

    switch (party) {
      case "Subject":
        findAddress = ".button-30";
        postCode = "#cicCaseAddress_cicCaseAddress_postcodeInput";
        selectAddress = "#cicCaseAddress_cicCaseAddress_addressList";
        buildingAndStreet = "#cicCaseAddress__detailAddressLine1";
        addressLine2 = "#cicCaseAddress__detailAddressLine2";
        addressLine3 = "#cicCaseAddress__detailAddressLine3";
        townOrCity = "#cicCaseAddress__detailPostTown";
        countyState = "#cicCaseAddress__detailCounty";
        country = "#cicCaseAddress__detailCountry";
        postcodeZipcode = "#cicCaseAddress__detailPostCode";
        break;
      default:
        findAddress = ".button-30";
        postCode = `#cicCase${party}Address_cicCase${party}Address_postcodeInput`;
        selectAddress = `#cicCase${party}Address_cicCase${party}Address_addressList`;
        buildingAndStreet = `#cicCase${party}Address__detailAddressLine1`;
        addressLine2 = `#cicCase${party}Address__detailAddressLine2`;
        addressLine3 = `#cicCase${party}Address__detailAddressLine3`;
        townOrCity = `#cicCase${party}Address__detailPostTown`;
        countyState = `#cicCase${party}Address__detailCounty`;
        country = `#cicCase${party}Address__detailCountry`;
        postcodeZipcode = `#cicCase${party}Address__detailPostCode`;
        break;
    }
    await page.fill(postCode, authors_content.postCode);
    await page.click(findAddress);
    await page.waitForSelector(selectAddress);
    await page.selectOption(selectAddress, authors_content.selectOption);
    expect(await page.inputValue(buildingAndStreet)).toEqual(
      authors_content.buildingAndStreet,
    );
    expect(await page.inputValue(addressLine2)).toEqual("");
    expect(await page.inputValue(addressLine3)).toEqual("");
    expect(await page.inputValue(townOrCity)).toEqual(
      authors_content.townOrCity,
    );
    expect(await page.inputValue(countyState)).toEqual("");
    expect(await page.inputValue(country)).toEqual(authors_content.country);
    expect(await page.inputValue(postcodeZipcode)).toEqual(
      authors_content.postCode,
    );
  },

  async convertDate(tab: boolean): Promise<string> {
    const dayOfBirth = subjectDetailsPage.dayOfBirth;
    const monthOfBirth = subjectDetailsPage.monthOfBirth;
    const yearOfBirth = subjectDetailsPage.yearOfBirth;
    const monthName = this.months[Number(monthOfBirth) - 1];
    if (tab) {
      return `${dayOfBirth} ${monthName.slice(0, 3)} ${yearOfBirth}`;
    } else {
      return `${dayOfBirth} ${monthName} ${yearOfBirth}`;
    }
  },

  async getTimestamp(): Promise<string> {
    const currentDate = new Date();
    let hours = currentDate.getHours();
    hours = hours % 12;
    hours = hours ? hours : 12;
    return `${currentDate.getDate()} ${this.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}, ${hours}:${this.padZero(
      currentDate.getMinutes(),
    )}`;
  },

  async uploadFileController(
    page: Page,
    selector: string,
    docNumber: number,
    documentCategory: documentCategory,
    file: string,
    docManagementUpload: boolean,
  ): Promise<void> {
    if (docNumber === 0) {
      if (docManagementUpload) {
        await expect(page.locator(".heading-h3")).toHaveText(
          uploadCaseDocuments_content.subTitle1,
        );
      } else {
        await expect(page.locator(".heading-h3")).toHaveText(
          caseDocumentsUploadObject_content.subSubTitle1,
        );
      }
      await expect(page.locator(".form-label").nth(0)).toHaveText(
        caseDocumentsUploadObject_content.textOnPage5,
      );
      await expect(page.locator(".form-label").nth(1)).toHaveText(
        caseDocumentsUploadObject_content.textOnPage6,
      );
      await expect(page.locator(".form-label").nth(2)).toHaveText(
        caseDocumentsUploadObject_content.textOnPage7,
      );
    } else {
      await new Promise((resolve) => setTimeout(resolve, 5000)); // Handle EXUI file rate limiting.
    }

    await page.selectOption(
      `#${selector}_${docNumber.toString()}_documentCategory`,
      documentCategory,
    );
    await page.fill(
      `#${selector}_${docNumber.toString()}_documentEmailContent`,
      `Lorem ipsum text ${documentCategory}`,
    );
    let fileUploadLocator = `#${selector}_${docNumber}_documentLink`;
    await page.locator(fileUploadLocator).setInputFiles(file);
    await page
      .locator(".error-message")
      .nth(docNumber)
      .waitFor({ state: "hidden" });
  },

  async checkVisibleAndPresent(locator: Locator, count: number): Promise<void> {
    const promises = Array.from({ length: count }, (_, i) => {
      return expect(locator.nth(i)).toBeVisible();
    });
    await Promise.all([promises, expect(locator).toHaveCount(count)]);
  },

  async chooseEventFromDropdown(page: Page, chosenEvent: string): Promise<any> {
    await page.waitForSelector("#next-step", { state: "visible" });
    await page.selectOption("#next-step", chosenEvent);
    await expect(page.getByRole("button", { name: "Go" })).toBeEnabled();
    await page.getByRole("button", { name: "Go" }).click({ force: true });
    await expect(page.locator("div.spinner-container")).toHaveCount(0);
    while (await page.isVisible("#next-step")) {
      await page.getByRole("button", { name: "Go" }).click();
      await expect(page.locator("div.spinner-container")).toHaveCount(0);
      await page.waitForTimeout(5000);
    }
  },

  async checkNumberAndSubject(
    page: Page,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      expect(
        page.locator(
          "ccd-case-header > div > ccd-label-field > dl > dt > ccd-markdown > div > markdown > h3",
        ),
      ).toHaveText(subjectName),
      expect(page.locator(".case-field").first()).toContainText(
        allTabTitles_content.pageTitle + caseNumber,
      ),
    ]);
  },

  async checkAllCaseTabs(
    page: Page,
    caseNumber: string,
    respondent: boolean,
    subjectName: string,
  ): Promise<void> {
    await this.checkNumberAndSubject(page, caseNumber, subjectName);
    if (respondent) {
      await Promise.all([
        Array.from({ length: 11 }, (_, index) => {
          const textOnPage = (allTabTitles_content as any)[`tab${index + 1}`];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.mat-tab-label-content:text-is("${textOnPage}")`),
            1,
          );
        }).filter(Boolean),
        Array.from({ length: 2 }, (_, index) => {
          const textOnPage = (allTabTitles_content as any)[`tab${index + 14}`];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.mat-tab-label-content:text-is("${textOnPage}")`),
            1,
          );
        }).filter(Boolean),
      ]);
    } else {
      await Promise.all([
        Array.from({ length: 15 }, (_, index) => {
          if (index !== 11) {
            // Exclude tab 12 (index 11)
            const textOnPage = (allTabTitles_content as any)[`tab${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(`.mat-tab-label-content:text-is("${textOnPage}")`),
              1,
            );
          }
        }).filter(Boolean),
      ]);
    }
  },

  async generateUrl(baseURL: string, caseNumber: string): Promise<string> {
    const caseNumberDigits = caseNumber.replace(/\D/g, "");
    return `${baseURL}/case-details/${caseNumberDigits}#History`;
  },

  async checkAndAcceptCookies(
    page: Page,
    cy: boolean,
    service: string,
  ): Promise<void> {
    switch (cy) {
      case true:
        if (service === "UC") {
          await Promise.all([
            expect(page.locator(".govuk-cookie-banner__heading")).toHaveText(
              CookiesContent.titleCy + CaseFinderContent.headerCy,
            ),
            ...Array.from({ length: 2 }, (_, index) => {
              const textOnPage = (CookiesContent as any)[
                `textOnPageCy${index + 1}`
              ];
              return expect(
                page.locator(".govuk-body").nth(index),
              ).toContainText(textOnPage);
            }),
          ]);
        }
        await page.locator(".govuk-button").nth(0).click();
        await page
          .getByRole("button", { name: "Cuddio'r neges cwcihon" })
          .click();
        break;
      default:
        if (service === "UC") {
          await Promise.all([
            expect(page.locator(".govuk-cookie-banner__heading")).toHaveText(
              CookiesContent.title + CaseFinderContent.header,
            ),
            ...Array.from({ length: 2 }, (_, index) => {
              const textOnPage = (CookiesContent as any)[
                `textOnPage${index + 1}`
              ];
              return expect(
                page.locator(".govuk-body").nth(index),
              ).toContainText(textOnPage);
            }),
          ]);
        }
        await page.locator(".govuk-button").nth(0).click();
        await page.getByRole("button", { name: "Hide this message" }).click();
        break;
    }
  },

  async feedbackBanner(
    page: Page,
    cy: boolean,
    landingPage: boolean,
  ): Promise<void> {
    switch (cy) {
      case true:
        if (landingPage) {
          await Promise.all([
            expect(page.locator(".govuk-phase-banner__text")).toContainText(
              feedbackBanner_content.feedbackBannerCy,
            ),
            expect(page.locator("a.govuk-link").nth(0)).toHaveText(
              feedbackBanner_content.feedbackLinkTextCy,
            ),
            expect(page.locator("a.govuk-link").nth(0)).toHaveAttribute(
              "href",
              feedbackBanner_content.feedbackLink + "?lang=cy",
            ),
          ]);
        } else {
          await Promise.all([
            expect(page.locator(".govuk-phase-banner__text")).toContainText(
              feedbackBanner_content.feedbackBannerCy,
            ),
            expect(page.locator("a.govuk-link").nth(3)).toHaveText(
              feedbackBanner_content.feedbackLinkTextCy,
            ),
            expect(
              await page.locator("a.govuk-link").nth(3).getAttribute("href"),
            ).toContain(feedbackBanner_content.feedbackLink),
          ]);
        }
        break;
      default:
        if (landingPage) {
          await Promise.all([
            expect(page.locator(".govuk-phase-banner__text")).toContainText(
              feedbackBanner_content.feedbackBanner,
            ),
            expect(page.locator("a.govuk-link").nth(0)).toHaveText(
              feedbackBanner_content.feedbackLinkText,
            ),
            expect(page.locator("a.govuk-link").nth(0)).toHaveAttribute(
              "href",
              feedbackBanner_content.feedbackLink,
            ),
          ]);
        } else {
          await Promise.all([
            expect(page.locator(".govuk-phase-banner__text")).toContainText(
              feedbackBanner_content.feedbackBanner,
            ),
            expect(page.locator("a.govuk-link").nth(3)).toHaveText(
              feedbackBanner_content.feedbackLinkText,
            ),
            expect(
              await page.locator("a.govuk-link").nth(3).getAttribute("href"),
            ).toContain(feedbackBanner_content.feedbackLink),
          ]);
        }
        break;
    }
  },

  async signOutAndGoToCase(
    page: Page,
    user: UserRole,
    baseURL: string,
    caseNumber: string,
  ): Promise<void> {
    await page.locator(`a:text-is(" Sign out ")`).click();
    await page.waitForTimeout(5000);
    await page.waitForLoadState("domcontentloaded");
    await idamLoginHelper.signInUser(page, user, baseURL);
    await page.waitForTimeout(2000);
    await page.goto(await this.generateUrl(baseURL, caseNumber));
    await page.waitForLoadState("domcontentloaded");
  },

  async checkForButtons(
    page: Page,
    continueButton: string,
    previous: string,
    cancel: string,
  ): Promise<void> {
    await Promise.all([
      page.locator(continueButton).isVisible(),
      page.locator(previous).isVisible(),
      page.locator(cancel).isVisible(),
    ]);
  },

  async checkCommonDocument(
    newPage: Page,
    caseNumber: string,
    caseNoticeType: CaseNoticeType,
    template: Template,
    editDraftJourney: boolean,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      this.checkVisibleAndPresent(
        newPage.locator(`span:text-is("${subjectName}")`),
        1,
      ),
      this.checkVisibleAndPresent(
        newPage.locator(`span:text-is("${caseNumber.replace(/-/g, "")}")`),
        1,
      ),
    ]);

    if (editDraftJourney) {
      await this.checkVisibleAndPresent(
        newPage.locator(
          `span:text-is("${editDraftOrderMainContent_content.editDescription}")`,
        ),
        1,
      );
    } else {
      await this.checkVisibleAndPresent(
        newPage.locator(
          `span:text-is("${finalDecisionMain_content.description}")`,
        ),
        1,
      );
    }

    if (template === "CIC14 – LO General Directions") {
      await Promise.all([
        this.checkVisibleAndPresent(
          newPage.locator(`span:text-is("Dated ")`),
          1,
        ),
        this.checkVisibleAndPresent(
          newPage.locator(`span:text-is("${await this.todayDateDoc()}")`),
          1,
        ),
      ]);
    } else {
      await this.checkVisibleAndPresent(
        newPage.locator(`span:text-is("Dated ${await this.todayDateDoc()}")`),
        1,
      );
    }
    if (template !== "CIC3 - Rule 27" && caseNoticeType !== null) {
      if (editDraftJourney) {
        await this.checkVisibleAndPresent(
          newPage.locator(
            `span:text-is("${editDraftAddDocumentFooter_content.editSignature}")`,
          ),
          1,
        );
      } else {
        await this.checkVisibleAndPresent(
          newPage.locator(
            `span:text-is("${addDocumentFooter_content.signature}")`,
          ),
          1,
        );
      }
    }
    if (template !== "CIC13 - Pro Forma Summons") {
      if (caseNoticeType !== null) {
        await this.checkVisibleAndPresent(
          newPage.locator(`span:text-is("${caseNoticeType}")`),
          1,
        );
      }
    }
  },

  async checkDocument(
    page: Page,
    template: Template,
    caseNumber: string,
    caseNoticeType: CaseNoticeType,
    editDraftJourney: boolean,
    subjectName: string,
  ): Promise<void> {
    const context = page.context();
    const [newPage] = await Promise.all([
      context.waitForEvent("page"),
      page.click(`ccd-read-document-field > button`, {
        modifiers: ["ControlOrMeta"],
      }),
    ]);
    await newPage.waitForLoadState("domcontentloaded");
    switch (template) {
      default:
        throw new Error("No template selected");
      case "CIC1 - Eligibility":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (eligibility as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC2 - Quantum":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (quantum as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC3 - Rule 27":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (rule27 as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC4 - Blank Decision Notice":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (blank as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC6 - General Directions":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (generalDirections as any)[
              `textOnPage${index + 1}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC7 - ME Dmi Reports":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (MeDmi as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC8 - ME Joint Instructions":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (MeJoint as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC8 - ME Joint Instruction":
        await Promise.all([
          ...Array.from({ length: 11 }, (_, index) => {
            const textOnPage = (MeJoint as any)[`textOnPage${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC10 - Strike Out Warning":
        await Promise.all([
          ...Array.from({ length: 12 }, (_, index) => {
            const textOnPage = (strikeoutWarning as any)[
              `textOnPage${index + 1}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC11 - Strike Out Decision Notice":
        await Promise.all([
          ...Array.from({ length: 10 }, (_, index) => {
            const textOnPage = (strikeoutNotice as any)[
              `textOnPage${index + 1}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
      case "CIC13 - Pro Forma Summons":
        await Promise.all([
          ...Array.from({ length: 10 }, (_, index) => {
            const textOnPage = (proFormaSummons as any)[
              `textOnPage${index + 1}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        if (caseNoticeType !== null) {
          await Promise.all([
            this.checkVisibleAndPresent(
              newPage.locator(`span:text-is("Fox Court")`),
              1,
            ),
            this.checkVisibleAndPresent(
              newPage.locator(
                `span:text-is("${createSummaryListingDetails_content.morningTime}")`,
              ),
              1,
            ),
          ]);
        }
        break;
      case "CIC14 – LO General Directions":
        await Promise.all([
          ...Array.from({ length: 15 }, (_, index) => {
            const textOnPage = (loGeneralDirections as any)[
              `textOnPage${index + 1}`
            ];
            return commonHelpers.checkVisibleAndPresent(
              newPage.locator(`span:text-is("${textOnPage}")`),
              1,
            );
          }),
          this.checkCommonDocument(
            newPage,
            caseNumber,
            caseNoticeType,
            template,
            editDraftJourney,
            subjectName,
          ),
        ]);
        break;
    }
    await newPage.close();
  },
};

export default commonHelpers;

export type parties = "Subject" | "Representative" | "Respondent" | "Applicant";

export type Category = "Assessment" | "Eligibility";

export type SubCategory =
  | "Fatal"
  | "Medical Re-opening"
  | "Minor"
  | "Paragraph 26"
  | "Sexual Abuse"
  | "Special Jurisdiction"
  | "Other";

export type ContactPreference = "Email" | "Post";

export type documentCategory =
  | "A - Application Form"
  | "A - First decision"
  | "A - Application for review"
  | "A - Review decision"
  | "A - Notice of Appeal"
  | "A - Evidence/correspondence from the Appellant"
  | "A - Correspondence from the CICA"
  | "TD - Direction / decision notices"
  | "B - Police evidence"
  | "C - GP records"
  | "C - Hospital records"
  | "C - Mental Health records"
  | "C - Expert evidence"
  | "C - Other medical records"
  | "D - DWP records"
  | "D - HMRC records"
  | "D - Employment records"
  | "D - Schedule of Loss"
  | "D - Counter Schedule"
  | "D - Other"
  | "E - Care plan"
  | "E - Local Authority/care records"
  | "E - Other"
  | "L - Linked docs"
  | "S - Witness Statement"
  | "TG - Application for an extension of time"
  | "TG - Application for a postponement"
  | "TG - Submission from appellant"
  | "TG - Submission from respondent"
  | "TG - Other"
  | "DSS Tribunal form uploaded documents"
  | "DSS Supporting uploaded documents"
  | "DSS Other information documents";

export type Scheme = "1996" | "2001" | "2008" | "2012";

export type caseRegion =
  | "Scotland"
  | "London"
  | "Midlands"
  | "North East"
  | "North West"
  | "Wales & South West";

export type caseRegionCode =
  | "1-London"
  | "11-Scotland"
  | "2-Midlands"
  | "3-North East"
  | "4-North West"
  | "5-South East"
  | "6-South West"
  | "7-Wales";

export type allEvents =
  | "Submit case (cic)"
  | "Create Case"
  | "Case: Build case"
  | "To link related cases"
  | "Case: Issue to respondent"
  | "Create Flag"
  | "Case: Hearing Options"
  | "Case: Clear Hearing Options"
  | "Hearings: Create listing"
  | "Hearings: Edit listing"
  | "Hearings: Postpone hearing"
  | "Hearings: Cancel hearing"
  | "Hearings: Create summary"
  | "Hearings: Edit summary"
  | "Link cases"
  | "Case: Panel Composition"
  | "Case: Edit Panel Composition"
  | "Case: Close case"
  | "Case: Edit CICA details"
  | "Case: Reinstate case"
  | "Case: Edit case details"
  | "Case: Edit case"
  | "Case: Contact parties"
  | "Case: CICA Contact parties"
  | "Stays: Create/edit stay"
  | "Stays: Remove stay"
  | "Refer case to judge"
  | "Refer case to legal officer"
  | "Decision: Issue final decision"
  | "Case: Add note"
  | "Orders: Create and send order"
  | "Orders: Create draft"
  | "Orders: Send order"
  | "Orders: Edit draft"
  | "Document management: Upload"
  | "Document management: Amend"
  | "Orders: Manage due date";

export type hearingType = "Case management" | "Final" | "Interlocutory";

export type hearingFormat =
  | "Face to Face"
  | "Hybrid"
  | "Video"
  | "Telephone"
  | "Paper";

export type hearingSession = "Morning" | "Afternoon" | "All day";

export type hearingVenues =
  | "East London Tribunal Hearing Centre-2 Clove Crescent, East India Dock London"
  | "Fox Court - London (Central) SSCS Tribunal-4th Floor, Fox Court, 30 Brooke Street, London"
  | "Aberdeen Tribunal Hearing Centre-AB1, 48 Huntly Street, Aberdeen, AB10 1SH"
  | "Glasgow Tribunals Centre-20 York Street, Glasgow"
  | "Dundee Tribunal Hearing Centre-Endeavour House, Ground Floor, 1 Greenmarket, Dundee, DD1 4QB"
  | "Birmingham Civil And Family Justice Centre-Priory Courts, 33 Bull Street"
  | "North Staffordshire Justice Centre - Magistrates-Ryecroft"
  | "Nottingham Magistrates Court-Carrington Street"
  | "Wolverhampton Social Security And Child Support Tribunal-Wolverhampton ASC, Norwich Union House, 31 Waterloo Road, WV1 4DJ"
  | "Bradford Tribunal Hearing Centre-Rushton Avenue"
  | "Leeds Employment Tribunal-4th floor, City Exchange, 11 Albion Street, LS1 5ES"
  | "North Shields County Court And Family Court-2nd Floor, Kings Court, Earl Grey Way, Royal Quays"
  | "Sheffield Magistrates Court-Castle Street"
  | "Birkenhead County Court And Family Court-76 Hamilton Street"
  | "Liverpool Civil And Family Court-Vernon Street, City Square"
  | "Ashford Tribunal Hearing Centre-County Square"
  | "Brighton Tribunal Hearing Centre-City Gate House, 185 Dyke Road"
  | "Chelmsford Justice Centre-Priory Place, New London Road, CM2 0PP"
  | "Ipswich Magistrates Court-Elm Street, Ipswich, IP1 2AP"
  | "Kings Lynn Crown Court (& Magistrates)-St Margaret's Place, College Lane"
  | "Kings Lynn Crown Court-St Margaret's Place, College Lane"
  | "Norwich Social Security And Child Support Tribunal-The Old Bakery, 115 Queens Road, NR1 3PL"
  | "Southend Magistrates' Court-The Court House, 80 Victoria Avenue, Southend On Sea, SS2 6EU"
  | "Bristol Magistrates Court-Marlborough Street, Bristol, BS1 3NU"
  | "Havant Justice Centre-The Court House, Elmleigh Road, Havant, Portsmouth, PO9 2AL"
  | "Plymouth As St Catherine's House-St Catherine's House, 5 Notte Street Plymouth Devon"
  | "Taunton Magistrates Court-St John's Road"
  | "Cardiff Social Security And Child Support Tribunal-Cardiff Eastgate House, 35-43, Newport Road"
  | "Port Talbot Justice Centre - Family-Harbourside Road";

export type hearingVenueNames =
  | "Sheffield Magistrates Court"
  | "Liverpool Civil And Family Court"
  | "Aberdeen Tribunal Hearing Centre"
  | "Birmingham Civil And Family Justice Centre"
  | "East London Tribunal Hearing Centre"
  | "Cardiff Social Security And Child Support Tribunal"
  | "Bristol Magistrates Court"
  | "Fox Court"
  | "Brighton Tribunal Hearing Centre";

export type hearingOutcome =
  | "Adjourned"
  | "Allowed"
  | "Refused"
  | "Withdrawn at Hearing";

export type hearingAdjournedReasons =
  | "Adjourned to face to face"
  | "Adjourned to Video"
  | "Admin error"
  | "Appellant did not attend"
  | "Appellant did not have bundle"
  | "Appellant not ready to proceed"
  | "Complex case"
  | "Failure to comply with directions"
  | "For Legal Rep/No Sol"
  | "For Other Parties to Attend"
  | "Further evidence received at hearing"
  | "Further evidence supplied but not before Tribunal at hearing"
  | "Further Loss of Earnings information required - Appellant"
  | "Further Loss of Earnings information required - Respondent"
  | "Further medical evidence required - Appellant"
  | "Further medical evidence required - Respondent"
  | "Further police evidence required - Respondent"
  | "Further police evidence required - Appellant"
  | "Further police evidence required - HMCTS (Summons)"
  | "Insufficient time"
  | "Interpreter required"
  | "Member Unable to Attend"
  | "PO did not attend"
  | "Poor Evidence"
  | "Venue not suitable"
  | "Witness did not attend"
  | "Other";

export type hearingCancelledReasons =
  | "Case Rejected"
  | "Consent Order received and no time for infill"
  | "Incomplete Panel"
  | "No suitable cases that are ready to list"
  | "Request for R27 decision and no time for infill"
  | "Venue Unavailable"
  | "Other";

export type hearingPostponedReasons =
  | "Appellant is out of country"
  | "Appellant seeking legal advice"
  | "Appellant unable to attend face to face, change of hearing format requested"
  | "Appellant unavailable (holiday/work/appointment/unwell)"
  | "Bereavement"
  | "Case stayed due to Civil proceedings"
  | "CICA requests case be heard by a single Judge as a Rule 27 decision"
  | "CICA seeking Counsel"
  | "Extension granted"
  | "Face to face hearing required"
  | "Last minute submissions i.e. 1-2 weeks prior to hearing"
  | "Linked cases - to be heard together"
  | "Member excluded - listed in error"
  | "Representative/Solicitor cannot make contact with Appellant"
  | "Representative/Solicitor seeking further evidence"
  | "Representative/Solicitor unavailable (holiday/work/appointment/unwell)"
  | "Tribunal members unavailable (holiday/work/appointment/unwell)"
  | "Tribunal members deemed listing time directed inadequate"
  | "Other";

export type CaseNoticeType = "CaseManagement" | "Final" | null;

export type State =
  | "DSS-Submitted"
  | "Submitted"
  | "Case Management"
  | "Ready to list"
  | "Awaiting Hearing"
  | "Awaiting Outcome"
  | "Case closed"
  | "Case Stayed";

export type taskCompletionMethod =
  | "Link: Assign Task to Me and Go To Task"
  | "Link: Assign Task to Me"
  | "Event DropDown";
