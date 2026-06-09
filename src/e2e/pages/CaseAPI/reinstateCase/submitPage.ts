import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import createListingNotifyPageContent from "../../../fixtures/content/CaseAPI/createListing/createListingNotifyPage_content.ts";
import reinstateReason_content from "../../../fixtures/content/CaseAPI/reinstateCase/reinstateReason_content.ts";
import reinstateUploadDocument_content from "../../../fixtures/content/CaseAPI/reinstateCase/reinstateUploadDocument_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/reinstateCase/submit_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import { ReinstateReason } from "./reinstateReasonPage.ts";

type SubmitPage = {
  continue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    optionalText: boolean,
    subjectName: string,
  ): Promise<void>;
  checkValidInfo(
    page: Page,
    reinstateReason: ReinstateReason,
    optionalText: boolean,
  ): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const submitPage: SubmitPage = {
  continue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    optionalText: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.heading-h2:text-is("${submit_content.pageTitle}")`,
    );
    if (optionalText) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage3}")`),
        1,
      );
    }
    await Promise.all([
      expect(page.locator(".govuk-heading-l")).toHaveText(
        submit_content.pageHint,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`markdown > h3:text-is("${subjectName}")`),
        1,
      ),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        createListingNotifyPageContent.caseReference + caseNumber,
      ),
      ...Array.from({ length: 2 }, (_, index: number) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 1}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
      ...Array.from({ length: 5 }, (_, index: number) => {
        const textOnPage = (submit_content as any)[`textOnPage${index + 4}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${submit_content.textOnPage9}")`),
        4,
      ),
    ]);
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async checkValidInfo(
    page: Page,
    reinstateReason: ReinstateReason,
    optionalText: boolean,
  ): Promise<void> {
    switch (reinstateReason) {
      case "requestFollowingAWithdrawalDecision":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("Request following a withdrawal decision")`,
          ),
          1,
        );
        break;
      case "RequestFollowingAStrikeOutDecision":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("Request following a strike out decision")`,
          ),
          1,
        );
        break;
      case "caseHadBeenClosedInError":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("Case had been closed in error")`),
          1,
        );
        break;
      case "requestFollowingADecisionFromTheUpperTribunal":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("Request following a decision from the Upper Tribunal")`,
          ),
          1,
        );
        break;
      case "requestFollowingAnOralHearingApplicationFollowingARule27Decision":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("Request following an oral hearing application following a Rule 27 decision")`,
          ),
          1,
        );
        break;
      case "Request to set aside a tribunal decision following an oral hearing":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("Request to set aside a tribunal decision following an oral hearing")`,
          ),
          1,
        );
        break;
      default: // other
        await commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("Other")`),
          1,
        );
        break;
    }
    if (optionalText) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.ng-valid > div > .ng-valid > div > ccd-read-text-area-field > span:text-is("${reinstateReason_content.optionalText}")`,
        ),
        1,
      );
    }
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.text-16:text-is("${reinstateUploadDocument_content.uploadedDocumentCategory}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `span:text-is("${reinstateUploadDocument_content.uploadedDocumentDescription}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.ng-star-inserted:text-is("${path.basename(config.testPdfFile)}")`,
        ),
        1,
      ),
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

  async continueOn(page: Page): Promise<void> {
    await page.click(this.continue);
  },
};

export default submitPage;
