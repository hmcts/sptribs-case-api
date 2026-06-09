import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseSubjectDetailsObjectContent from "../../../fixtures/content/CaseAPI/editCase/editCaseSubjectDetailsObject_content.ts";
import authors_content from "../../../fixtures/content/authors_content.ts";
import commonHelpers, {
  ContactPreference,
} from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseSubjectDetailsObjectPage = {
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
  checkFields(
    page: Page,
    initialState: initialState,
    subjectName: string,
  ): Promise<void>;
  fillInFields(
    page: Page,
    contactPreference: ContactPreference,
    initialState: initialState,
    subjectName: string,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseSubjectDetailsObjectPage: EditCaseSubjectDetailsObjectPage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  findAddress: ".button-30",
  fullName: "#cicCaseFullName",
  phoneNumber: "#cicCasePhoneNumber",
  day: "#cicCaseDateOfBirth-day",
  month: "#cicCaseDateOfBirth-month",
  year: "#cicCaseDateOfBirth-year",
  emailAddress: "#cicCaseEmail",
  selectEmail: "#cicCaseContactPreferenceType-Email",
  selectPost: "#cicCaseContactPreferenceType-Post",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseSubjectDetailsObjectContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseSubjectDetailsObjectContent.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        editCaseSubjectDetailsObjectContent.pageTitle,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseSubjectDetailsObjectContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 7 }, (_, index) => {
        const textOnPage = (editCaseSubjectDetailsObjectContent as any)[
          `textOnPage${index + 1}`
        ];
        return expect(page.locator(".form-label").nth(index)).toHaveText(
          textOnPage,
        );
      }),
      expect(page.locator(".heading-h2")).toHaveText(
        editCaseSubjectDetailsObjectContent.subTitle1,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (editCaseSubjectDetailsObjectContent as any)[
          `textOnPage${index + 8}`
        ];
        return expect(page.locator(".form-label").nth(index + 14)).toHaveText(
          textOnPage,
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

  async checkFields(
    page: Page,
    initialState: initialState,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      expect(page.locator(this.fullName)).toHaveValue(subjectName),
      expect(page.locator(this.phoneNumber)).toHaveValue(
        editCaseSubjectDetailsObjectContent.contactNumber,
      ),
      expect(page.locator(this.day)).toHaveValue(
        "0" + editCaseSubjectDetailsObjectContent.dayOfBirth,
      ),
      expect(page.locator(this.year)).toHaveValue(
        editCaseSubjectDetailsObjectContent.yearOfBirth,
      ),
      expect(page.getByLabel("Email", { exact: true })).toBeChecked(),
      expect(page.locator(this.emailAddress)).toHaveValue(
        editCaseSubjectDetailsObjectContent.emailAddress,
      ),
    ]);
    if (initialState !== "DSS Submitted") {
      await Promise.all([
        expect(page.locator(this.month)).toHaveValue(
          "0" + editCaseSubjectDetailsObjectContent.monthOfBirth,
        ),
        expect(page.locator("#cicCaseAddress__detailAddressLine1")).toHaveValue(
          authors_content.buildingAndStreet,
        ),
        expect(page.locator("#cicCaseAddress__detailPostTown")).toHaveValue(
          authors_content.townOrCity,
        ),
        expect(page.locator("#cicCaseAddress__detailCountry")).toHaveValue(
          authors_content.country,
        ),
        expect(page.locator("#cicCaseAddress__detailPostCode")).toHaveValue(
          authors_content.postCode,
        ),
      ]);
    } else {
      await expect(page.locator(this.month)).toHaveValue("01");
    }
  },

  async fillInFields(
    page: Page,
    contactPreference: ContactPreference,
    initialState: initialState,
    subjectName: string,
  ): Promise<void> {
    await page.fill(this.fullName, `${subjectName}`);
    await page.fill(
      this.phoneNumber,
      editCaseSubjectDetailsObjectContent.contactNumber,
    );
    await page.fill(this.day, editCaseSubjectDetailsObjectContent.dayOfBirth);
    await page.fill(
      this.month,
      editCaseSubjectDetailsObjectContent.monthOfBirth,
    );
    await page.fill(this.year, editCaseSubjectDetailsObjectContent.yearOfBirth);
    if (initialState === "DSS Submitted") {
      await commonHelpers.postcodeHandler(page, "Subject");
    }
    if (contactPreference === "Email") {
      await page.click(this.selectEmail);
      await page.fill(
        this.emailAddress,
        editCaseSubjectDetailsObjectContent.emailAddress,
      );
    } else if (contactPreference === "Post") {
      await page.click(this.selectPost);
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.locator(this.fullName).clear();
    await page.locator(this.phoneNumber).clear();
    await page.locator(this.day).clear();
    await page.locator(this.month).clear();
    await page.locator(this.year).clear();
    await page.locator(this.emailAddress).clear();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseSubjectDetailsObjectContent.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        editCaseSubjectDetailsObjectContent.nameError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        editCaseSubjectDetailsObjectContent.nameError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        editCaseSubjectDetailsObjectContent.dobError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        editCaseSubjectDetailsObjectContent.dobError,
      ),
      expect(page.locator(".validation-error").nth(2)).toHaveText(
        editCaseSubjectDetailsObjectContent.addressError,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        editCaseSubjectDetailsObjectContent.postcodeError,
      ),
      expect(page.locator(".validation-error").nth(3)).toHaveText(
        editCaseSubjectDetailsObjectContent.emailError,
      ),
      expect(page.locator(".error-message").nth(3)).toHaveText(
        editCaseSubjectDetailsObjectContent.streetError,
      ),
      expect(page.locator(".error-message").nth(4)).toHaveText(
        editCaseSubjectDetailsObjectContent.emailError,
      ),
    ]);
    await page.fill(this.day, "90");
    await page.click(this.month);
    await expect(page.locator(".error-message").nth(1)).toHaveText(
      editCaseSubjectDetailsObjectContent.invalidDOBError,
    );
    await page.locator(this.day).clear();
    await page.fill("#cicCaseAddress_cicCaseAddress_postcodeInput", "...");
    await page.click(this.findAddress);
    await expect(page.locator(".error-message").nth(2)).toHaveText(
      editCaseSubjectDetailsObjectContent.validPostcodeError,
    );
  },
};

export default editCaseSubjectDetailsObjectPage;
