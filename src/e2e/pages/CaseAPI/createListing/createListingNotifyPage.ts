import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateListingNotifyPage = {
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

const createListingNotifyPage: CreateListingNotifyPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${createListingNotifyPageContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        createListingNotifyPageContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),

      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `dt > ccd-markdown > div > markdown > p:text-is("${createListingNotifyPageContent.textOnPage1}")`,
        ),
        1,
      ),
    ]);

    if (!DSSSubmitted) {
      await Promise.all([
        ...Array.from({ length: 4 }, (_, index: number) => {
          const textOnPage = (createListingNotifyPageContent as any)[
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
      await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).isVisible()
    ) {
      await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).check();
      await expect(
        page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`),
      ).toBeChecked();
    }
    if (
      await page
        .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
        .isVisible()
    ) {
      await page
        .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
        .check();
      await expect(
        page.locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`),
      ).toBeChecked();
    }
    if (
      await page
        .locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`)
        .isVisible()
    ) {
      await page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`).check();
      await expect(
        page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`),
      ).toBeChecked();
    }
    if (
      await page
        .locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`)
        .isVisible()
    ) {
      await page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`).check();
      await expect(
        page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`),
      ).toBeChecked();
    }
    await page.getByRole("button", { name: "Continue" }).click();
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.getByRole("button", { name: "Continue" }).click();
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-heading:has-text("${createListingNotifyPageContent.errorTitle}")`,
      ),
      1,
    );
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-list:has-text("${createListingNotifyPageContent.errorMessage}")`,
      ),
      1,
    );
  },
};

export default createListingNotifyPage;
