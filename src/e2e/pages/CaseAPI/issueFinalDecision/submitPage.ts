import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import caseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/createCase/caseSubjectDetailsObject_content.ts";
import addDocumentFooter_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/addDocumentFooter_content.ts";
import decisionUpload_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/decisionUpload_content.ts";
import finalDecisionMain_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/finalDecisionMain_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/issueFinalDecision/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { NoticeType } from "./noticeOptionPage.ts";
import { Template } from "./selectTemplatePage.ts";

type SubmitPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkCommon(page: Page, caseNumber: string): Promise<void>;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    noticeType: NoticeType,
  ): Promise<void>;
  checkCommonInfo(page: Page): Promise<void>;
  checkAllInfo(
    page: Page,
    noticeType: NoticeType,
    template: Template,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkCommon(page: Page, caseNumber: string): Promise<void> {
    await page.waitForSelector(`span:text-is("${submit_content.textOnPage1}")`);
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.govuk-heading-l:text-is("${submit_content.pageHint}")`),
        1,
      ),
      expect(page.locator("markdown > h3")).toContainText(
        caseSubjectDetailsObject_content.name,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span:text-is("${submit_content.textOnPage2}")`),
        4,
      ),
      commonHelpers.checkForButtons(
        page,
        this.continue,
        this.previous,
        this.cancel,
      ),
    ]);
  },

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    noticeType: NoticeType,
  ): Promise<void> {
    switch (noticeType) {
      default:
        throw new Error("No notice type selected");
      case "upload":
        await Promise.all([
          ...Array.from({ length: 2 }, (_, index) => {
            const upload = (submit_content as any)[`upload${index + 2}`];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(`.text-16:text-is("${upload}")`),
              1,
            );
          }),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${submit_content.upload1}")`),
            2,
          ),
          this.checkCommon(page, caseNumber),
        ]);
        break;
      case "Create":
        await Promise.all([
          ...Array.from({ length: 3 }, (_, index) => {
            const create = (submit_content as any)[`create${index + 1}`];
            return commonHelpers.checkVisibleAndPresent(
              page.locator(`.text-16:text-is("${create}")`),
              1,
            );
          }),
          this.checkCommon(page, caseNumber),
        ]);
        break;
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkCommonInfo(page: Page): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Subject")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Representative")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("Respondent")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("Applicant (if different from subject)")`,
        ),
        1,
      ),
    ]);
  },

  async checkAllInfo(
    page: Page,
    noticeType: NoticeType,
    template: Template,
  ): Promise<void> {
    switch (noticeType) {
      default:
        throw new Error("No notice type selected.");
      case "upload":
        await Promise.all([
          this.checkCommonInfo(page),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("Upload from your computer")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `span:text-is("${decisionUpload_content.description}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.ng-star-inserted:text-is("${path.basename(config.testPdfFile)}")`,
            ),
            1,
          ),
        ]);
        break;
      case "Create":
        await Promise.all([
          this.checkCommonInfo(page),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("Create from a template")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${template}")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `span:text-is("${finalDecisionMain_content.description}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${addDocumentFooter_content.signature}")`,
            ),
            1,
          ),
        ]);
        break;
    }
  },

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
