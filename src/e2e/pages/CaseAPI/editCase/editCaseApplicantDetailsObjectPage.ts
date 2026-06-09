import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseApplicantDetailsObjectContent from "../../../fixtures/content/CaseAPI/editCase/editCaseApplicantDetailsObject_content.ts";
import commonHelpers, {
  ContactPreference,
} from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseApplicantDetailsObjectPage = {
  previous: string;
  continue: string;
  cancel: string;
  findAddress: string;
  fullName: string;
  phoneNumber: string;
  day: string;
  month: string;
  year: string;
  emailAddress: string;
  selectEmail: string;
  selectPost: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkFields(page: Page, initialState: initialState): Promise<void>;
  fillInFields(page: Page, contactPreference: ContactPreference): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseApplicantDetailsObjectPage: EditCaseApplicantDetailsObjectPage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  findAddress: ".button-30",
  fullName: "#cicCaseApplicantFullName",
  phoneNumber: "#cicCaseApplicantPhoneNumber",
  day: "#cicCaseApplicantDateOfBirth-day",
  month: "#cicCaseApplicantDateOfBirth-month",
  year: "#cicCaseApplicantDateOfBirth-year",
  emailAddress: "#cicCaseApplicantEmailAddress",
  selectEmail: "#cicCaseApplicantContactDetailsPreference-Email",
  selectPost: "#cicCaseApplicantContactDetailsPreference-Post",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseApplicantDetailsObjectContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseApplicantDetailsObjectContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseApplicantDetailsObjectContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 9 }, (_, index) => {
        const textOnPage = (editCaseApplicantDetailsObjectContent as any)[
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
      await new AxeUtils(page).audit();
    }
  },

  async checkFields(page: Page, initialState: initialState): Promise<void> {
    if (initialState !== "DSS Submitted") {
      await Promise.all([
        expect(page.locator(this.fullName)).toHaveValue(
          editCaseApplicantDetailsObjectContent.name,
        ),
        expect(page.locator(this.phoneNumber)).toHaveValue(
          editCaseApplicantDetailsObjectContent.contactNumber,
        ),
        expect(page.locator(this.day)).toHaveValue(
          "0" + editCaseApplicantDetailsObjectContent.dayOfBirth,
        ),
        expect(page.locator(this.month)).toHaveValue(
          "0" + editCaseApplicantDetailsObjectContent.monthOfBirth,
        ),
        expect(page.locator(this.year)).toHaveValue(
          editCaseApplicantDetailsObjectContent.yearOfBirth,
        ),
        expect(page.getByLabel("Email", { exact: true })).toBeChecked(),
        expect(page.locator(this.emailAddress)).toHaveValue(
          editCaseApplicantDetailsObjectContent.emailAddress,
        ),
      ]);
    }
  },

  async fillInFields(
    page: Page,
    contactPreference: ContactPreference,
  ): Promise<void> {
    await page.fill(this.fullName, editCaseApplicantDetailsObjectContent.name);
    await page.fill(
      this.phoneNumber,
      editCaseApplicantDetailsObjectContent.contactNumber,
    );
    await page.fill(this.day, editCaseApplicantDetailsObjectContent.dayOfBirth);
    await page.fill(
      this.month,
      editCaseApplicantDetailsObjectContent.monthOfBirth,
    );
    await page.fill(
      this.year,
      editCaseApplicantDetailsObjectContent.yearOfBirth,
    );
    if (contactPreference === "Email") {
      await page.click(this.selectEmail);
      await page.click(this.selectEmail); // needs to double-click due to EXUI
      await expect(page.locator(".form-label").nth(17)).toHaveText(
        editCaseApplicantDetailsObjectContent.textOnPage10,
      );
      await page.fill(
        this.emailAddress,
        editCaseApplicantDetailsObjectContent.emailAddress,
      );
    } else if (contactPreference === "Post") {
      await page.click(this.selectPost);
      await page.click(this.selectPost);
      await Promise.all([
        expect(page.locator(".heading-h2")).toHaveText(
          editCaseApplicantDetailsObjectContent.subTitle1,
        ),
        expect(page.locator(".form-label").nth(9)).toHaveText(
          editCaseApplicantDetailsObjectContent.textOnPage11,
        ),
        expect(page.locator(".manual-link")).toHaveText(
          editCaseApplicantDetailsObjectContent.linkOnPage1,
        ),
      ]);
      await commonHelpers.postcodeHandler(page, "Applicant");
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseApplicantDetailsObjectContent.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        editCaseApplicantDetailsObjectContent.nameError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        editCaseApplicantDetailsObjectContent.phoneNumberError,
      ),
      expect(page.locator(".validation-error").nth(2)).toHaveText(
        editCaseApplicantDetailsObjectContent.contactError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        editCaseApplicantDetailsObjectContent.nameError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        editCaseApplicantDetailsObjectContent.phoneNumberError,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        editCaseApplicantDetailsObjectContent.contactError,
      ),
    ]);
    await page.fill(this.phoneNumber, "abc");
    await expect(page.locator(".error-message").nth(1)).toHaveText(
      editCaseApplicantDetailsObjectContent.validPhoneNumberError,
    );
    await page.locator(this.phoneNumber).clear();
    await page.getByLabel("Email", { exact: true }).click();
    await page.click(this.emailAddress);
    await page.locator(this.continue).dispatchEvent("click");
    await Promise.all([
      expect(page.locator(".error-message").last()).toHaveText(
        editCaseApplicantDetailsObjectContent.emailError,
      ),
    ]);
    await page.getByLabel("Post", { exact: true }).click();
    await page.click(this.findAddress);
    await Promise.all([
      expect(page.locator(".error-message").nth(2)).toHaveText(
        editCaseApplicantDetailsObjectContent.postcodeError,
      ),
    ]);
    await page.fill(
      "#cicCaseApplicantAddress_cicCaseApplicantAddress_postcodeInput",
      "...",
    );
    await page.click(this.findAddress);
    await expect(page.locator(".error-message").nth(2)).toHaveText(
      editCaseApplicantDetailsObjectContent.validPostcodeError,
    );
    await page.locator(this.continue).dispatchEvent("click");
    await Promise.all([
      expect(page.locator(".validation-error").last()).toHaveText(
        editCaseApplicantDetailsObjectContent.addressError,
      ),
      expect(page.locator(".error-message").last()).toHaveText(
        editCaseApplicantDetailsObjectContent.streetError,
      ),
    ]);
  },
};

export default editCaseApplicantDetailsObjectPage;
