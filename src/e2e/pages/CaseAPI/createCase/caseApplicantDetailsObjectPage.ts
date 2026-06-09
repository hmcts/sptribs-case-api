import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseApplicantDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseApplicantDetailsObject_content.ts";
import commonHelpers, {
  ContactPreference,
} from "../../../helpers/commonHelpers.ts";

type CaseApplicantDetailsObjectPage = {
  continue: string;
  findAddress: string;
  fullName: string;
  phoneNumber: string;
  day: string;
  month: string;
  year: string;
  emailAddress: string;
  selectEmail: string;
  selectPost: string;
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(page: Page, contactPreference: ContactPreference): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const caseApplicantDetailsObjectPage: CaseApplicantDetailsObjectPage = {
  continue: '[type="submit"]',
  findAddress: ".button-30",
  fullName: "#cicCaseApplicantFullName",
  phoneNumber: "#cicCaseApplicantPhoneNumber",
  day: "#cicCaseApplicantDateOfBirth-day",
  month: "#cicCaseApplicantDateOfBirth-month",
  year: "#cicCaseApplicantDateOfBirth-year",
  emailAddress: "#cicCaseApplicantEmailAddress",
  selectEmail: "#cicCaseApplicantContactDetailsPreference-Email",
  selectPost: "#cicCaseApplicantContactDetailsPreference-Post",

  async checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${caseApplicantDetailsObject_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        caseApplicantDetailsObject_content.pageHint,
      ),
      ...Array.from({ length: 9 }, (_, index) => {
        const textOnPage = (caseApplicantDetailsObject_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(
    page: Page,
    contactPreference: ContactPreference,
  ): Promise<void> {
    await page.fill(this.fullName, caseApplicantDetailsObject_content.name);
    await page.fill(
      this.phoneNumber,
      caseApplicantDetailsObject_content.contactNumber,
    );
    await page.fill(this.day, caseApplicantDetailsObject_content.dayOfBirth);
    await page.fill(
      this.month,
      caseApplicantDetailsObject_content.monthOfBirth,
    );
    await page.fill(this.year, caseApplicantDetailsObject_content.yearOfBirth);
    if (contactPreference === "Email") {
      await page.click(this.selectEmail);
      await page.click(this.selectEmail); // needs to double-click due to EXUI
      await expect(page.locator(".form-label").nth(17)).toHaveText(
        caseApplicantDetailsObject_content.textOnPage10,
      );
      await page.fill(
        this.emailAddress,
        caseApplicantDetailsObject_content.emailAddress,
      );
    } else if (contactPreference === "Post") {
      await page.click(this.selectPost);
      await page.click(this.selectPost);
      await Promise.all([
        expect(page.locator(".heading-h2")).toHaveText(
          caseApplicantDetailsObject_content.subTitle1,
        ),
        expect(page.locator(".form-label").nth(9)).toHaveText(
          caseApplicantDetailsObject_content.textOnPage11,
        ),
        expect(page.locator(".manual-link")).toHaveText(
          caseApplicantDetailsObject_content.linkOnPage1,
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
        caseApplicantDetailsObject_content.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        caseApplicantDetailsObject_content.nameError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        caseApplicantDetailsObject_content.phoneNumberError,
      ),
      expect(page.locator(".validation-error").nth(2)).toHaveText(
        caseApplicantDetailsObject_content.contactError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        caseApplicantDetailsObject_content.nameError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        caseApplicantDetailsObject_content.phoneNumberError,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        caseApplicantDetailsObject_content.contactError,
      ),
    ]);
    await page.fill(this.phoneNumber, "abc");
    await expect(page.locator(".error-message").nth(1)).toHaveText(
      caseApplicantDetailsObject_content.validPhoneNumberError,
    );
    await page.locator(this.phoneNumber).clear();
    await page.getByLabel("Email", { exact: true }).click();
    await page.click(this.emailAddress);
    await page.locator(this.continue).dispatchEvent("click");
    await Promise.all([
      expect(page.locator(".error-message").last()).toHaveText(
        caseApplicantDetailsObject_content.emailError,
      ),
    ]);
    await page.getByLabel("Post", { exact: true }).click();
    await page.click(this.findAddress);
    await Promise.all([
      expect(page.locator(".error-message").nth(2)).toHaveText(
        caseApplicantDetailsObject_content.postcodeError,
      ),
    ]);
    await page.fill(
      "#cicCaseApplicantAddress_cicCaseApplicantAddress_postcodeInput",
      "...",
    );
    await page.click(this.findAddress);
    await expect(page.locator(".error-message").nth(2)).toHaveText(
      caseApplicantDetailsObject_content.validPostcodeError,
    );
    await page.locator(this.continue).dispatchEvent("click");
    await Promise.all([
      expect(page.locator(".validation-error").last()).toHaveText(
        caseApplicantDetailsObject_content.addressError,
      ),
      expect(page.locator(".error-message").last()).toHaveText(
        caseApplicantDetailsObject_content.streetError,
      ),
    ]);
  },
};

export default caseApplicantDetailsObjectPage;
