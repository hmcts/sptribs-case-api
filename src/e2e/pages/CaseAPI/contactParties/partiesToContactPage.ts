import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import partiesToContact_content from "../../../fixtures/content/CaseAPI/contactParties/partiesToContact_content.ts";
import selectDocument_content from "../../../fixtures/content/CaseAPI/contactParties/selectDocument_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type PartiesToContactPage = {
  continue: string;
  message: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    user: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  tickCheckBoxes(page: Page, checkBoxes: boolean, user: string): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const partiesToContactPage: PartiesToContactPage = {
  continue: '[type="submit"]',
  message: "#cicCaseNotifyPartyMessage",
  previous: ".button-secondary[disabled]",
  cancel: ".cancel",
  async checkPageLoads(
    page: Page,
    caseNumber: string,
    user: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    const pageHintRegex = new RegExp(
      `${selectDocument_content.pageHint}|${partiesToContact_content.pageHintCICA}`,
    );
    const textToCheck =
      user === "respondent"
        ? partiesToContact_content.textOnPage7
        : partiesToContact_content.textOnPage4;
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${partiesToContact_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toContainText(pageHintRegex),
      expect(page.locator("markdown > h3").nth(0)).toContainText(
        `${subjectName}`,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        partiesToContact_content.caseReference + caseNumber,
      ),
      expect(page.locator("markdown > p").nth(1)).toContainText(
        partiesToContact_content.textOnPage,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`text="${partiesToContact_content.textOnPage5}"`),
        4,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (partiesToContact_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
      expect(page.locator("markdown > p").nth(1)).toContainText(
        partiesToContact_content.textOnPage,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
    await commonHelpers.checkVisibleAndPresent(
      await page.locator(`.form-label:text-is("${textToCheck}")`),
      1,
    );
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await page.waitForSelector("#error-summary-title");
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        partiesToContact_content.errorBanner1,
      ),
      expect(page.locator(".validation-error")).toHaveText(
        partiesToContact_content.messageRequiredError1,
      ),
      expect(page.locator(".error-message")).toHaveText(
        partiesToContact_content.messageRequiredError1,
      ),
    ]);
    await page.fill(this.message, partiesToContact_content.message);
    await page.click(this.continue);
    await page.waitForSelector(
      `div.error-summary > h3:has-text("${partiesToContact_content.errorBanner2}")`,
    );
    await expect(
      page.locator(
        `#errors > li:has-text("${partiesToContact_content.partyRequiredError}")`,
      ),
    ).toBeVisible();
  },

  async fillInFields(page) {
    await page.fill(this.message, partiesToContact_content.message);
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },

  async tickCheckBoxes(
    page: Page,
    checkBoxes: boolean,
    user: string,
  ): Promise<void> {
    let checkboxNames: string[] = [];
    if (user == "respondent") {
      checkboxNames = [
        "contactParties_subjectContactParties",
        "contactParties_applicantContactParties",
        "contactParties_representativeContactParties",
        "contactParties_tribunal",
      ];
    } else {
      checkboxNames = [
        "cicCaseNotifyPartySubject",
        "cicCaseNotifyPartyApplicant",
        "cicCaseNotifyPartyRepresentative",
        "cicCaseNotifyPartyRespondent",
      ];
    }
    for (const name of checkboxNames) {
      const checkboxLocator = page.locator(
        `input[type="checkbox"][name="${name}"]`,
      );
      await checkboxLocator.waitFor({ state: "visible" });
      const isPresent = await checkboxLocator.count();
      if (isPresent > 0) {
        if (checkBoxes) {
          await checkboxLocator.check();
          await checkboxLocator.isChecked();
        } else {
          await checkboxLocator.uncheck();
        }
      }
    }
  },
};
export default partiesToContactPage;
