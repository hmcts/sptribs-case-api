import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCaseFurtherDetailsObjectContent from "../../../fixtures/content/CaseAPI/editCase/editCaseFurtherDetailsObject_content.ts";
import commonHelpers, {
  caseRegion,
  Scheme,
} from "../../../helpers/commonHelpers.ts";
import { initialState } from "../../../journeys/CaseAPI/editCase.ts";

type editCaseDSSFurtherDetailsObjectPage = {
  previous: string;
  continue: string;
  cancel: string;
  scheme: string;
  caseRegion: string;
  CICAReferenceNumber: string;
  claimLinkedYes: string;
  claimLinkedNo: string;
  compensationLinkedYes: string;
  compensationLinkedNo: string;
  tribunalFormInTimeYes: string;
  tribunalFormInTimeNo: string;
  explainedYes: string;
  explainedNo: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkFields(page: Page, initialState: initialState): Promise<void>;
  fillInFields(
    page: Page,
    schemeSelection: Scheme,
    caseRegionSelection: caseRegion,
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
  ): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editCaseFurtherDetailsObjectPage: editCaseDSSFurtherDetailsObjectPage = {
  previous: "button[name='Previous']",
  continue: '[type="submit"]',
  cancel: ".cancel",
  scheme: "#cicCaseSchemeCic",
  caseRegion: "#cicCaseRegionCIC",
  CICAReferenceNumber: "#cicCaseCicaReferenceNumber",
  claimLinkedYes: "#cicCaseClaimLinkedToCic_Yes",
  claimLinkedNo: "#cicCaseClaimLinkedToCic_No",
  compensationLinkedYes: "#cicCaseCompensationClaimLinkCIC_Yes",
  compensationLinkedNo: "#cicCaseCompensationClaimLinkCIC_No",
  tribunalFormInTimeYes: "#cicCaseFormReceivedInTime_Yes",
  tribunalFormInTimeNo: "#cicCaseFormReceivedInTime_No",
  explainedYes: "#cicCaseMissedTheDeadLineCic_Yes",
  explainedNo: "#cicCaseMissedTheDeadLineCic_No",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCaseFurtherDetailsObjectContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCaseFurtherDetailsObjectContent.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCaseFurtherDetailsObjectContent.caseReference + caseNumber,
      ),
      expect(page.locator(".form-label").nth(0)).toHaveText(
        editCaseFurtherDetailsObjectContent.textOnPage1,
      ),
      expect(page.locator(".form-label").nth(1)).toHaveText(
        editCaseFurtherDetailsObjectContent.textOnPage2,
      ),
      expect(page.locator(".form-label").nth(2)).toHaveText(
        editCaseFurtherDetailsObjectContent.textOnPage3,
      ),
      expect(page.locator(".form-label").nth(3)).toHaveText(
        editCaseFurtherDetailsObjectContent.yes,
      ),
      expect(page.locator(".form-label").nth(4)).toHaveText(
        editCaseFurtherDetailsObjectContent.no,
      ),
      expect(page.locator(".form-label").nth(5)).toHaveText(
        editCaseFurtherDetailsObjectContent.textOnPage5,
      ),
      expect(page.locator(".form-label").nth(6)).toHaveText(
        editCaseFurtherDetailsObjectContent.yes,
      ),
      expect(page.locator(".form-label").nth(7)).toHaveText(
        editCaseFurtherDetailsObjectContent.no,
      ),
      expect(page.locator(".form-label").nth(8)).toHaveText(
        editCaseFurtherDetailsObjectContent.textOnPage6,
      ),
      expect(page.locator(".form-label").nth(9)).toHaveText(
        editCaseFurtherDetailsObjectContent.yes,
      ),
      expect(page.locator(".form-label").nth(10)).toHaveText(
        editCaseFurtherDetailsObjectContent.no,
      ),
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
        expect(page.locator(this.scheme)).toHaveValue("1: Preference"),
        expect(page.locator(this.caseRegion)).toHaveValue("1: Scotland"),
        expect(page.locator(this.claimLinkedYes)).toBeChecked(),
        expect(page.locator(this.CICAReferenceNumber)).toHaveValue("1"),
        expect(page.locator(this.compensationLinkedYes)).toBeChecked(),
        expect(page.locator(this.tribunalFormInTimeYes)).toBeChecked(),
      ]);
    }
  },

  async fillInFields(
    page: Page,
    schemeSelection: Scheme,
    caseRegionSelection: caseRegion,
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
  ): Promise<void> {
    await page.selectOption(this.scheme, schemeSelection);
    await page.selectOption(this.caseRegion, caseRegionSelection);
    if (claimsLinked) {
      await page.click(this.claimLinkedYes);
      await expect(page.locator(".form-label").nth(5)).toHaveText(
        editCaseFurtherDetailsObjectContent.textOnPage4,
      );
      await page.fill(this.CICAReferenceNumber, "1");
    } else {
      await page.click(this.claimLinkedNo);
    }
    if (compensationLinked) {
      await page.click(this.compensationLinkedYes);
    } else {
      await page.click(this.compensationLinkedNo);
    }
    if (tribunalFormsInTime) {
      await page.click(this.tribunalFormInTimeYes);
    } else {
      await page.click(this.tribunalFormInTimeNo);
      await Promise.all([
        expect(page.locator(".form-label").nth(12)).toHaveText(
          editCaseFurtherDetailsObjectContent.textOnPage7,
        ),
        expect(page.locator(".form-label").nth(13)).toHaveText(
          editCaseFurtherDetailsObjectContent.yes,
        ),
        expect(page.locator(".form-label").nth(14)).toHaveText(
          editCaseFurtherDetailsObjectContent.no,
        ),
      ]);

      if (applicantExplained) {
        await page.click(this.explainedYes);
      } else {
        await page.click(this.explainedNo);
      }
    }
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator("#error-summary-title")).toHaveText(
        editCaseFurtherDetailsObjectContent.errorBanner,
      ),
      expect(page.locator(".validation-error").nth(0)).toHaveText(
        editCaseFurtherDetailsObjectContent.schemeError,
      ),
      expect(page.locator(".validation-error").nth(1)).toHaveText(
        editCaseFurtherDetailsObjectContent.regionError,
      ),
      expect(page.locator(".validation-error").nth(2)).toHaveText(
        editCaseFurtherDetailsObjectContent.linksError,
      ),
      expect(page.locator(".validation-error").nth(3)).toHaveText(
        editCaseFurtherDetailsObjectContent.compensationError,
      ),
      expect(page.locator(".validation-error").nth(4)).toHaveText(
        editCaseFurtherDetailsObjectContent.tribunalError,
      ),
      expect(page.locator(".error-message").nth(0)).toHaveText(
        editCaseFurtherDetailsObjectContent.schemeError,
      ),
      expect(page.locator(".error-message").nth(1)).toHaveText(
        editCaseFurtherDetailsObjectContent.regionError,
      ),
      expect(page.locator(".error-message").nth(2)).toHaveText(
        editCaseFurtherDetailsObjectContent.linksError,
      ),
      expect(page.locator(".error-message").nth(3)).toHaveText(
        editCaseFurtherDetailsObjectContent.compensationError,
      ),
      expect(page.locator(".error-message").nth(4)).toHaveText(
        editCaseFurtherDetailsObjectContent.tribunalError,
      ),
    ]);
    await page.click(this.claimLinkedYes);
    await page.click(this.tribunalFormInTimeNo);
    await page.fill(this.CICAReferenceNumber, "1");
    await page.locator(this.CICAReferenceNumber).clear();
    await page.click(this.continue);
    await Promise.all([
      expect(page.locator(".error-message").nth(2)).toHaveText(
        editCaseFurtherDetailsObjectContent.CICAError,
      ),
      expect(page.locator(".error-message").nth(4)).toHaveText(
        editCaseFurtherDetailsObjectContent.deadlineError,
      ),
    ]);
  },
};

export default editCaseFurtherDetailsObjectPage;
