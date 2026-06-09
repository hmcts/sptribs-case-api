import { expect, Page } from "@playwright/test";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import finalDecisionMain_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/finalDecisionMain_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { Template } from "./selectTemplatePage.ts";

type FinalDecisionMainPage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    template: Template,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
};

const finalDecisionMainPage: FinalDecisionMainPage = {
  previous: ".button-secondary",
  continue: '[type="submit"]',
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    template: Template,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${finalDecisionMain_content.pageTitle}")`,
    );
    await Promise.all([
      expect(page.locator(".govuk-caption-l")).toHaveText(
        finalDecisionMain_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `markdown > h3:text-is("${caseSubjectDetailsObject_content.name}")`,
        ),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        finalDecisionMain_content.caseReference + caseNumber,
      ),
      ...Array.from({ length: 3 }, (_, index) => {
        const textOnPage = (finalDecisionMain_content as any)[
          `textOnPage${index + 1}`
        ];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`markdown:has-text("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 3 }, (_, index) => {
        const subTitle = (finalDecisionMain_content as any)[
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
    switch (template) {
      default:
        await expect(page.locator(`textarea`)).toBeEmpty();
        break;
      case "CIC1 - Eligibility":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(
          `${finalDecisionMain_content.eligibility}`,
        );
        break;
      case "CIC2 - Quantum":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(`${finalDecisionMain_content.quantum}`);
        break;
      case "CIC3 - Rule 27":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(`${finalDecisionMain_content.rule27}`);
        break;
      case "CIC7 - ME Dmi Reports":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(`${finalDecisionMain_content.dmiReports}`);
        break;
      case "CIC8 - ME Joint Instructions":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(`${finalDecisionMain_content.joint}`);
        break;
      case "CIC10 - Strike Out Warning":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(
          `${finalDecisionMain_content.strikeoutWarn}`,
        );
        break;
      case "CIC11 - Strike Out Decision Notice":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(
          `${finalDecisionMain_content.strikeoutNotice}`,
        );
        break;
      case "CIC13 - Pro Forma Summons":
        textBoxValue = await page.locator(`textarea`).inputValue();
        expect(textBoxValue).toEqual(`${finalDecisionMain_content.proForma}`);
        break;
    }
    // if (accessibilityTest) {
    //   await new AxeUtils(page).audit();
    // }
  },

  async fillInFields(page: Page): Promise<void> {
    await page.fill(`textarea`, ``);
    await expect(page.locator(`textarea`)).toBeEmpty();
    await page.fill(`textarea`, finalDecisionMain_content.description);
    await page.click(this.continue);
  },

  async triggerErrorMessages(page: Page): Promise<void> {
    await page.fill(`textarea`, ``);
    await expect(page.locator(`textarea`)).toBeEmpty();
    await page.click(this.continue);
    await commonHelpers.checkVisibleAndPresent(
      page.locator(
        `.error-message:has-text("${finalDecisionMain_content.errorNoEntryDescription}")`,
      ),
      1,
    );
    await this.fillInFields(page);
  },
};

export default finalDecisionMainPage;
