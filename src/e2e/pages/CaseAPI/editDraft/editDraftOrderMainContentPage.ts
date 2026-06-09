import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editDraftOrderMainContentContent from "../../../fixtures/content/CaseAPI/editDraft/editDraftOrderMainContent_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditDraftOrderMainContentPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const editDraftOrderMainContentPage: EditDraftOrderMainContentPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${editDraftOrderMainContentContent.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        editDraftOrderMainContentContent.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        editDraftOrderMainContentContent.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown:has-text("${editDraftOrderMainContentContent.textOnPage1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `p:has-text("${editDraftOrderMainContentContent.textOnPage2}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown:has-text("${editDraftOrderMainContentContent.textOnPage3}")`,
        ),
        1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const subTitle = (editDraftOrderMainContentContent as any)[
          `subTitle${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`h3:text-is("${subTitle}")`),
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
    let textBoxValue = "";
    textBoxValue = await page.locator(`textarea`).inputValue();
    expect(textBoxValue).toEqual(
      `${editDraftOrderMainContentContent.description}`,
    );
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.fill(`textarea`, ``);
    await expect(page.locator(`textarea`)).toBeEmpty();
    await page.fill(
      `textarea`,
      editDraftOrderMainContentContent.editDescription,
    );
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.fill(`textarea`, ``);
    await expect(page.locator(`textarea`)).toBeEmpty();
    await page.click(this.continue);
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-message:has-text("${editDraftOrderMainContentContent.errorNoEntryDescription}")`,
      ),
      1,
    );
    await this.fillInFields(page);
  },
};

export default editDraftOrderMainContentPage;
