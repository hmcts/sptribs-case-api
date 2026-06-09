import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editListingNotifyPageContent from "../../../fixtures/content/CaseAPI/editListing/editListingNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditListingNotifyPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editListingNotifyPage: EditListingNotifyPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editListingNotifyPageContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editListingNotifyPageContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editListingNotifyPageContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `dt > ccd-markdown > div > markdown > p:text-is("${editListingNotifyPageContent.textOnPage1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${editListingNotifyPageContent.textOnPage2}")`,
        ),
        4,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (editListingNotifyPageContent as any)[
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
  },

  async continueOn(page: Page): Promise<void> {
    await expect(
      page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`),
    ).toBeChecked();
    await expect(
      page.locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`),
    ).toBeChecked();
    await expect(
      page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`),
    ).toBeChecked();
    await expect(
      page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`),
    ).toBeChecked();
    await page.getByRole("button", { name: "Continue" }).click();
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).click();
    await page
      .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
      .click();
    await page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`).click();
    await page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`).click();
    await page.getByRole("button", { name: "Continue" }).click();
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-heading:has-text("${editListingNotifyPageContent.errorTitle}")`,
      ),
      1,
    );
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-summary-list:has-text("${editListingNotifyPageContent.errorMessage}")`,
      ),
      1,
    );
    await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).click();
    await page
      .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
      .click();
    await page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`).click();
    await page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`).click();
  },
};

export default editListingNotifyPage;
