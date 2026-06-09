import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editCicaCaseDetails_content from "../../../fixtures/content/CaseAPI/editCase/editCICACaseDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditCicaCaseDetailsPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editCicaCaseDetailsPage: EditCicaCaseDetailsPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary[disabled]",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editCicaCaseDetails_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editCicaCaseDetails_content.pageHint,
      ),
      expect(page.locator("markdown > h3")).toContainText(subjectName),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editCicaCaseDetails_content.caseReference + caseNumber,
      ),

      expect(page.locator(".form-label").nth(0)).toHaveText(
        editCicaCaseDetails_content.textOnPage1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (editCicaCaseDetails_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.form-label:text-is("${textOnPage}")`),
          1,
        );
      }),
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

  async fillFields(page: Page): Promise<void> {
    await page.fill(
      "#editCicaCaseDetails_cicaReferenceNumber",
      editCicaCaseDetails_content.referenceNumber,
    );
    await page.fill(
      "#editCicaCaseDetails_cicaCaseWorker",
      editCicaCaseDetails_content.caseWorker,
    );
    await page.fill(
      "#editCicaCaseDetails_cicaCasePresentingOfficer",
      editCicaCaseDetails_content.presentingOfficer,
    );
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default editCicaCaseDetailsPage;
