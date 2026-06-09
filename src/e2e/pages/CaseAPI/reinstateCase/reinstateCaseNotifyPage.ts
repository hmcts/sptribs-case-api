import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import reinstateCaseNotifyPage_content from "../../../fixtures/content/CaseAPI/reinstateCase/reinstateCaseNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type ReinstateCaseNotifyPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const reinstateCaseNotifyPage: ReinstateCaseNotifyPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${reinstateCaseNotifyPage_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        reinstateCaseNotifyPage_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `dt > ccd-markdown > div > markdown > p:text-is("${reinstateCaseNotifyPage_content.textOnPage1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${reinstateCaseNotifyPage_content.textOnPage2}")`,
        ),
        4,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (reinstateCaseNotifyPage_content as any)[
          `textOnPage${index + 3}`
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
    ``;
  },

  async continueOn(page: Page): Promise<void> {
    const casePartyNotifySubject = page.locator(
      `#cicCaseNotifyPartySubject-SubjectCIC`,
    );
    const casePartyNotifyRepresentative = page.locator(
      `#cicCaseNotifyPartyRepresentative-RepresentativeCIC`,
    );
    const casePartyNotifyRespondent = page.locator(
      `#cicCaseNotifyPartyRespondent-RespondentCIC`,
    );
    const casePartyNotifyApplicant = page.locator(
      `#cicCaseNotifyPartyApplicant-ApplicantCIC`,
    );
    if (!(await casePartyNotifySubject.isChecked())) {
      await casePartyNotifySubject.click();
    }
    if (!(await casePartyNotifyRepresentative.isChecked())) {
      await casePartyNotifyRepresentative.click();
    }
    if (!(await casePartyNotifyRespondent.isChecked())) {
      await casePartyNotifyRespondent.click();
    }
    if (!(await casePartyNotifyApplicant.isChecked())) {
      await casePartyNotifyApplicant.click();
    }
    await page.getByRole("button", { name: "Continue" }).click();
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    const casePartyNotifySubject = page.locator(
      `#cicCaseNotifyPartySubject-SubjectCIC`,
    );
    const casePartyNotifyRepresentative = page.locator(
      `#cicCaseNotifyPartyRepresentative-RepresentativeCIC`,
    );
    const casePartyNotifyRespondent = page.locator(
      `#cicCaseNotifyPartyRespondent-RespondentCIC`,
    );
    const casePartyNotifyApplicant = page.locator(
      `#cicCaseNotifyPartyApplicant-ApplicantCIC`,
    );
    if (await casePartyNotifySubject.isChecked()) {
      await casePartyNotifySubject.click();
    }
    if (await casePartyNotifyRepresentative.isChecked()) {
      await casePartyNotifyRepresentative.click();
    }
    if (await casePartyNotifyRespondent.isChecked()) {
      await casePartyNotifyRespondent.click();
    }
    if (await casePartyNotifyApplicant.isChecked()) {
      await casePartyNotifyApplicant.click();
    }
    await page.getByRole("button", { name: "Continue" }).click();
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-heading:has-text("${reinstateCaseNotifyPage_content.errorTitle}")`,
      ),
      1,
    );
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-list:has-text("${reinstateCaseNotifyPage_content.errorMessage}")`,
      ),
      1,
    );
  },
};

export default reinstateCaseNotifyPage;
