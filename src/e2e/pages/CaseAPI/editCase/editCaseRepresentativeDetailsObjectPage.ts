import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseRepresentativeDetailsObjectContent from "../../../fixtures/content/CaseAPI/editCase/editCaseRepresentativeDetailsObject_content.ts";
import commonHelpers, {
  ContactPreference,
} from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type EditCaseRepresentativeDetailsObjectPage = {
  previous: string;
  continue: string;
  cancel: string;
  findAddress: string;
  fullName: string;
  orgName: string;
  phoneNumber: string;
  reference: string;
  qualifiedYes: string;
  qualifiedNo: string;
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
  fillInFields(
    page: Page,
    contactPreference: ContactPreference,
    representativeQualified: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseRepresentativeDetailsObjectPage: EditCaseRepresentativeDetailsObjectPage =
  {
    previous: "button[name='Previous']",
    continue: '[type="submit"]',
    cancel: ".cancel",
    findAddress: ".button-30",
    fullName: "#cicCaseRepresentativeFullName",
    orgName: "#cicCaseRepresentativeOrgName",
    phoneNumber: "#cicCaseRepresentativePhoneNumber",
    reference: "#cicCaseRepresentativeReference",
    qualifiedYes: "#cicCaseIsRepresentativeQualified_Yes",
    qualifiedNo: "#cicCaseIsRepresentativeQualified_No",
    emailAddress: "#cicCaseRepresentativeEmailAddress",
    selectEmail: "#cicCaseRepresentativeContactDetailsPreference-Email",
    selectPost: "#cicCaseRepresentativeContactDetailsPreference-Post",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForSelector(
        `.govuk-heading-l:text-is("${editCaseRepresentativeDetailsObjectContent.pageTitle}")`,
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          editCaseRepresentativeDetailsObjectContent.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          editCaseRepresentativeDetailsObjectContent.pageTitle,
        ),
        expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          editCaseRepresentativeDetailsObjectContent.caseReference + caseNumber,
        ),
        ...Array.from({ length: 10 }, (_, index) => {
          const textOnPage = (
            editCaseRepresentativeDetailsObjectContent as any
          )[`textOnPage${index + 1}`];
          return expect(page.locator(".form-label").nth(index)).toHaveText(
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

    async checkFields(page: Page, initialState: initialState): Promise<void> {
      if (initialState !== "DSS Submitted") {
        await Promise.all([
          expect(page.locator(this.fullName)).toHaveValue(
            editCaseRepresentativeDetailsObjectContent.name,
          ),
          expect(page.locator(this.orgName)).toHaveValue(
            editCaseRepresentativeDetailsObjectContent.organisation,
          ),
          expect(page.locator(this.phoneNumber)).toHaveValue(
            editCaseRepresentativeDetailsObjectContent.contactNumber,
          ),
          expect(page.getByLabel("Yes", { exact: true })).toBeChecked(),
          expect(page.getByLabel("Email", { exact: true })).toBeChecked(),
          expect(page.locator(this.emailAddress)).toHaveValue(
            editCaseRepresentativeDetailsObjectContent.emailAddress,
          ),
        ]);
      }
    },

    async fillInFields(
      page: Page,
      contactPreference: ContactPreference,
      representativeQualified: boolean,
    ): Promise<void> {
      await page.fill(
        this.fullName,
        editCaseRepresentativeDetailsObjectContent.name,
      );
      await page.fill(
        this.orgName,
        editCaseRepresentativeDetailsObjectContent.organisation,
      );
      await page.fill(
        this.phoneNumber,
        editCaseRepresentativeDetailsObjectContent.contactNumber,
      );
      if (representativeQualified) {
        await page.click(this.qualifiedYes);
      } else {
        await page.click(this.qualifiedNo);
      }
      if (contactPreference === "Email") {
        await page.click(this.selectEmail);
        await page.click(this.selectEmail); // needs to double-click due to EXUI
        await expect(page.locator(".form-label").nth(10)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.textOnPage11,
        );
        await page.fill(
          this.emailAddress,
          editCaseRepresentativeDetailsObjectContent.emailAddress,
        );
      } else if (contactPreference === "Post") {
        await page.click(this.selectPost);
        await page.click(this.selectPost);
        await Promise.all([
          expect(page.locator(".heading-h2")).toHaveText(
            editCaseRepresentativeDetailsObjectContent.subTitle1,
          ),
          expect(page.locator(".form-label").nth(10)).toHaveText(
            editCaseRepresentativeDetailsObjectContent.textOnPage11,
          ),
          expect(page.locator(".manual-link")).toHaveText(
            editCaseRepresentativeDetailsObjectContent.linkOnPage1,
          ),
        ]);
        await commonHelpers.postcodeHandler(page, "Representative");
      }
      await page.click(this.continue);
    },

    async triggerErrorMessages(page: Page): Promise<void> {
      await page.locator(this.fullName).clear();
      await page.locator(this.phoneNumber).clear();
      await page.locator(this.emailAddress).clear();
      await page.click(this.continue);
      await Promise.all([
        expect(page.locator("#error-summary-title")).toHaveText(
          editCaseRepresentativeDetailsObjectContent.errorBanner,
        ),
        expect(page.locator(".validation-error").nth(0)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.nameError,
        ),
        expect(page.locator(".validation-error").nth(1)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.phoneNumberError,
        ),
        expect(page.locator(".validation-error").nth(2)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.emailError,
        ),
        expect(page.locator(".error-message").nth(0)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.nameError,
        ),
        expect(page.locator(".error-message").nth(1)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.phoneNumberError,
        ),
        expect(page.locator(".error-message").nth(2)).toHaveText(
          editCaseRepresentativeDetailsObjectContent.emailError,
        ),
      ]);
      await page.fill(this.phoneNumber, "abc");
      await expect(page.locator(".error-message").nth(1)).toHaveText(
        editCaseRepresentativeDetailsObjectContent.validPhoneNumberError,
      );
      await page.locator(this.phoneNumber).clear();
      await page.getByLabel("Post", { exact: true }).click();
      await page.click(this.findAddress);
      await expect(page.locator(".error-message").last()).toHaveText(
        editCaseRepresentativeDetailsObjectContent.postcodeError,
      );
      await page.fill(
        "#cicCaseRepresentativeAddress_cicCaseRepresentativeAddress_postcodeInput",
        "...",
      );
      await page.click(this.findAddress);
      await expect(page.locator(".error-message").last()).toHaveText(
        editCaseRepresentativeDetailsObjectContent.validPostcodeError,
      );
      await page.locator(this.continue).dispatchEvent("click");
      await Promise.all([
        expect(page.locator(".validation-error").last()).toHaveText(
          editCaseRepresentativeDetailsObjectContent.addressError,
        ),
        expect(page.locator(".error-message").last()).toHaveText(
          editCaseRepresentativeDetailsObjectContent.streetError,
        ),
      ]);
    },
  };

export default editCaseRepresentativeDetailsObjectPage;
