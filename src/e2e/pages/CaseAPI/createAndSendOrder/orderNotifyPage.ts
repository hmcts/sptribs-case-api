import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import orderNotifyPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/orderNotifyPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type OrderNotifyPage = {
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const orderNotifyPage: OrderNotifyPage = {
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `dt > ccd-markdown > div > markdown > p:text-is("${orderNotifyPage_content.textOnPage1}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        orderNotifyPage_content.pageTitle,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        orderNotifyPage_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.form-label:text-is("${orderNotifyPage_content.textOnPage2}")`,
        ),
        4,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (orderNotifyPage_content as any)[
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
    if (!(await page.isChecked(`#cicCaseNotifyPartySubject-SubjectCIC`))) {
      await page.locator(`#cicCaseNotifyPartySubject-SubjectCIC`).click();
      await page
        .locator(`#cicCaseNotifyPartyRepresentative-RepresentativeCIC`)
        .click();
      await page.locator(`#cicCaseNotifyPartyRespondent-RespondentCIC`).click();
      await page.locator(`#cicCaseNotifyPartyApplicant-ApplicantCIC`).click();
    }
    await page.getByRole("button", { name: "Continue" }).click();
  },
};

export default orderNotifyPage;
