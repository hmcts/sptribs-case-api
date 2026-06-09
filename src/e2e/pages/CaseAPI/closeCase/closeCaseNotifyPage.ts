import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import closeCaseNotifyPage_content from "../../../fixtures/content/CaseAPI/closeCase/closeCaseNotifyPage_content.ts";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CloseCaseNotifyPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const closeCaseNotifyPage: CloseCaseNotifyPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${closeCaseNotifyPage_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        closeCaseNotifyPage_content.pageHint,
      ),
      expect(page.locator(".govuk-heading-l")).toHaveText(
        closeCaseNotifyPage_content.pageTitle,
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
          `dt > ccd-markdown > div > markdown > p:text-is("${closeCaseNotifyPage_content.textOnPage1}")`,
        ),
        1,
      ),
    ]);
    if (!DSSSubmitted) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.form-label:text-is("${closeCaseNotifyPage_content.textOnPage2}")`,
          ),
          4,
        ),
        ...Array.from({ length: 4 }, (_, index: number) => {
          const textOnPage = (closeCaseNotifyPage_content as any)[
            `textOnPage${index + 3}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.form-label:text-is("${textOnPage}")`),
            1,
          );
        }),
      ]);
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async continueOn(page: Page): Promise<void> {
    if (
      (await page
        .locator(`#cicCaseNotifyPartySubject-SubjectCIC`)
        .isChecked()) ||
      (await page
        .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
        .isChecked())
    ) {
      await page.getByRole("button", { name: "Continue" }).click();
    } else {
      if (
        await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).isVisible()
      ) {
        await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).click();
      }
      if (
        await page
          .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
          .isVisible()
      ) {
        await page
          .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
          .click();
      }
      if (
        await page
          .locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`)
          .isVisible()
      ) {
        await page
          .locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`)
          .click();
      }
      if (
        await page
          .locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`)
          .isVisible()
      ) {
        await page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`).click();
      }
      await page.getByRole("button", { name: "Continue" }).click();
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.getByRole("button", { name: "Continue" }).click();
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-heading:has-text("${closeCaseNotifyPage_content.errorTitle}")`,
      ),
      1,
    );
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-list:has-text("${closeCaseNotifyPage_content.errorMessage}")`,
      ),
      1,
    );
  },
};

export default closeCaseNotifyPage;
