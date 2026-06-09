import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import orderMainContentPage_content from "../../../fixtures/content/CaseAPI/createAndSendOrder/orderMainContentPage_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { Template } from "../issueFinalDecision/selectTemplatePage.ts";

type OrderMainContentPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    template: Template,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const orderMainContentPage: OrderMainContentPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    template: Template,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${orderMainContentPage_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        orderMainContentPage_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        orderMainContentPage_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown:has-text("${orderMainContentPage_content.textOnPage1}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `p:has-text("${orderMainContentPage_content.textOnPage2}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown:has-text("${orderMainContentPage_content.textOnPage3}")`,
        ),
        1,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const subTitle = (orderMainContentPage_content as any)[
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
    switch (template) {
      default:
        await expect(page.locator(`textarea`)).toBeEmpty();
        break;
      case "CIC3 - Rule 27":
        expect(textBoxValue).toEqual(`${orderMainContentPage_content.rule27}`);
        break;
      case "CIC7 - ME Dmi Reports":
        expect(textBoxValue).toEqual(
          `${orderMainContentPage_content.dmiReports}`,
        );
        break;
      case "CIC8 - ME Joint Instruction":
        expect(textBoxValue).toEqual(`${orderMainContentPage_content.joint}`);
        break;
      case "CIC10 - Strike Out Warning":
        expect(textBoxValue).toEqual(
          `${orderMainContentPage_content.strikeoutWarn}`,
        );
        break;
      case "CIC13 - Pro Forma Summons":
        expect(textBoxValue).toEqual(
          `${orderMainContentPage_content.proForma}`,
        );
        break;
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.fill(`textarea`, ``);
    await expect(page.locator(`textarea`)).toBeEmpty();
    await page.fill(`textarea`, orderMainContentPage_content.description);
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.fill(`textarea`, ``);
    await expect(page.locator(`textarea`)).toBeEmpty();
    await page.click(this.continue);
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-message:has-text("${orderMainContentPage_content.errorNoEntryDescription}")`,
      ),
      1,
    );
    await this.fillInFields(page);
  },
};

export default orderMainContentPage;
