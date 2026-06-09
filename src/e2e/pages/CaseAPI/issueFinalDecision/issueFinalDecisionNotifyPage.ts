import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import issueFinalDecisionNotifyPage_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/issueFinalDecisionNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type IssueFinalDecisionNotifyPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const issueFinalDecisionNotifyPage: IssueFinalDecisionNotifyPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${issueFinalDecisionNotifyPage_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        issueFinalDecisionNotifyPage_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h3:text-is("${caseSubjectDetailsObject_content.name}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `dt > ccd-markdown > div > markdown > p:text-is("${issueFinalDecisionNotifyPage_content.textOnPage1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${issueFinalDecisionNotifyPage_content.textOnPage2}")`,
        ),
        4,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (issueFinalDecisionNotifyPage_content as any)[
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
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-summary-heading:has-text("${issueFinalDecisionNotifyPage_content.errorTitle}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.error-summary-list:has-text("${issueFinalDecisionNotifyPage_content.errorMessage}")`,
        ),
        1,
      ),
    ]);
    await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).click();
    await page
      .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
      .click();
    await page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`).click();
    await page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`).click();
    await this.continueOn(page);
  },
};

export default issueFinalDecisionNotifyPage;
