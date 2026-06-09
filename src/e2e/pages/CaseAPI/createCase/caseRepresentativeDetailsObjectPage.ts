import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseRepresentativeDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseRepresentativeDetailsObject_content.ts";
import commonHelpers, {
  ContactPreference,
} from "../../../helpers/commonHelpers.ts";

type CaseRepresentativeDetailsObjectPage = {
  continue: string;
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
  checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  fillInFields(
    page: Page,
    contactPreference: ContactPreference,
    representativeQualified: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const caseRepresentativeDetailsObjectPage: CaseRepresentativeDetailsObjectPage =
  {
    continue: '[type="submit"]',
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
      accessibilityTest: boolean,
    ): Promise<void> {
      await page.waitForSelector(
        `.govuk-heading-l:text-is("${caseRepresentativeDetailsObject_content.pageTitle}")`,
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          caseRepresentativeDetailsObject_content.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          caseRepresentativeDetailsObject_content.pageTitle,
        ),
        ...Array.from({ length: 10 }, (_, index) => {
          const textOnPage = (caseRepresentativeDetailsObject_content as any)[
            `textOnPage${index + 1}`
          ];
          return expect(page.locator(".form-label").nth(index)).toHaveText(
            textOnPage,
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
      representativeQualified: boolean,
    ): Promise<void> {
      await page.fill(
        this.fullName,
        caseRepresentativeDetailsObject_content.name,
      );
      await page.fill(
        this.orgName,
        caseRepresentativeDetailsObject_content.organisation,
      );
      await page.fill(
        this.phoneNumber,
        caseRepresentativeDetailsObject_content.contactNumber,
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
          caseRepresentativeDetailsObject_content.textOnPage11,
        );
        await page.fill(
          this.emailAddress,
          caseRepresentativeDetailsObject_content.emailAddress,
        );
      } else if (contactPreference === "Post") {
        await page.click(this.selectPost);
        await page.click(this.selectPost);
        await Promise.all([
          expect(page.locator(".heading-h2")).toHaveText(
            caseRepresentativeDetailsObject_content.subTitle1,
          ),
          expect(page.locator(".form-label").nth(10)).toHaveText(
            caseRepresentativeDetailsObject_content.textOnPage11,
          ),
          expect(page.locator(".manual-link")).toHaveText(
            caseRepresentativeDetailsObject_content.linkOnPage1,
          ),
        ]);
        await commonHelpers.postcodeHandler(page, "Representative");
      }
      await page.click(this.continue);
    },

    async triggerErrorMessages(page: Page): Promise<void> {
      await page.click(this.continue);
      await Promise.all([
        expect(page.locator("#error-summary-title")).toHaveText(
          caseRepresentativeDetailsObject_content.errorBanner,
        ),
        expect(page.locator(".validation-error").nth(0)).toHaveText(
          caseRepresentativeDetailsObject_content.nameError,
        ),
        expect(page.locator(".validation-error").nth(1)).toHaveText(
          caseRepresentativeDetailsObject_content.phoneNumberError,
        ),
        expect(page.locator(".validation-error").nth(2)).toHaveText(
          caseRepresentativeDetailsObject_content.qualifiedError,
        ),
        expect(page.locator(".validation-error").nth(3)).toHaveText(
          caseRepresentativeDetailsObject_content.contactError,
        ),
        expect(page.locator(".error-message").nth(0)).toHaveText(
          caseRepresentativeDetailsObject_content.nameError,
        ),
        expect(page.locator(".error-message").nth(1)).toHaveText(
          caseRepresentativeDetailsObject_content.phoneNumberError,
        ),
        expect(page.locator(".error-message").nth(2)).toHaveText(
          caseRepresentativeDetailsObject_content.qualifiedError,
        ),
        expect(page.locator(".error-message").nth(3)).toHaveText(
          caseRepresentativeDetailsObject_content.contactError,
        ),
      ]);
      await page.fill(this.phoneNumber, "abc");
      await expect(page.locator(".error-message").nth(1)).toHaveText(
        caseRepresentativeDetailsObject_content.validPhoneNumberError,
      );
      await page.locator(this.phoneNumber).clear();
      await page.getByLabel("Email", { exact: true }).click();
      await page.click(this.emailAddress);
      await page.locator(this.continue).dispatchEvent("click");
      await expect(page.locator(".error-message").last()).toHaveText(
        caseRepresentativeDetailsObject_content.emailError,
      );
      await page.getByLabel("Post", { exact: true }).click();
      await page.click(this.findAddress);
      await expect(page.locator(".error-message").last()).toHaveText(
        caseRepresentativeDetailsObject_content.postcodeError,
      );
      await page.fill(
        "#cicCaseRepresentativeAddress_cicCaseRepresentativeAddress_postcodeInput",
        "...",
      );
      await page.click(this.findAddress);
      await expect(page.locator(".error-message").last()).toHaveText(
        caseRepresentativeDetailsObject_content.validPostcodeError,
      );
      await page.locator(this.continue).dispatchEvent("click");
      await Promise.all([
        expect(page.locator(".validation-error").last()).toHaveText(
          caseRepresentativeDetailsObject_content.addressError,
        ),
        expect(page.locator(".error-message").last()).toHaveText(
          caseRepresentativeDetailsObject_content.streetError,
        ),
      ]);
    },
  };

export default caseRepresentativeDetailsObjectPage;
