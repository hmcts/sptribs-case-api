import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createCaseLinkCreateCaseLink_content from "../../../fixtures/content/CaseAPI/LinkCase/createCaseLinkCreateCaseLink_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateCaseLinkCreateCaseLinkPage = {
  next: string;
  previous: string;
  submit: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  triggerErrorMessage(page: Page): Promise<void>;
};

const createCaseLinkCreateCaseLink: CreateCaseLinkCreateCaseLinkPage = {
  next: "#next-button",
  previous: ".button-secondary[disabled]",
  submit: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-xl:text-is("${createCaseLinkCreateCaseLink_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createCaseLinkCreateCaseLink_content.caseReference + caseNumber,
      ),
      expect(page.locator(".govuk-body").nth(0)).toHaveText(
        createCaseLinkCreateCaseLink_content.textOnPage1,
      ),
      expect(page.locator(".govuk-body").nth(1)).toHaveText(
        createCaseLinkCreateCaseLink_content.textOnPage2,
      ),
      page.locator(this.next).isVisible(),
      commonHelpers.checkForButtons(
        page,
        this.submit,
        this.previous,
        this.cancel,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.click(this.submit);
  },

  async triggerErrorMessage(page: Page): Promise<void> {
    await page.click(this.submit);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        createCaseLinkCreateCaseLink_content.errorBanner,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        createCaseLinkCreateCaseLink_content.errorMessage,
      ),
    ]);
  },
};

export default createCaseLinkCreateCaseLink;
