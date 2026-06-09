import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import {
  default as summaryTab_content,
  default as summaryTabContent,
} from "../../../fixtures/content/CaseAPI/caseTabs/summaryTab_content.ts";
import addStay_content from "../../../fixtures/content/CaseAPI/createEditStay/addStay_content.ts";
import createEditStaySubmit_content from "../../../fixtures/content/CaseAPI/createEditStay/submit_content.ts";
import removeStay_content from "../../../fixtures/content/CaseAPI/removeStay/removeStay_content.ts";
import removeStaySubmit_content from "../../../fixtures/content/CaseAPI/removeStay/submit_content.ts";
import representativeDetailsContent from "../../../fixtures/content/DSSCreateCase/RepresentativeDetails_content.ts";
import subjectContactDetailsContent from "../../../fixtures/content/DSSCreateCase/SubjectContactDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type SummaryTabPage = {
  summaryTab: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    representationPresent: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  changeToSummaryTab(page: Page): Promise<void>;
  checkPageInfo(
    page: Page,
    caseNumber: string,
    representationPresent: boolean,
    representationQualified: boolean,
    subjectName: string,
    outOfTimeDate?: Date,
  ): Promise<void>;
  checkStayDetails(
    page: Page,
    stayReason: keyof typeof createEditStaySubmit_content,
    optionalText: boolean,
    state: string,
  ): Promise<void>;
  checkRemoveStayDetails(
    page: Page,
    Remove: keyof typeof removeStaySubmit_content,
    optionalText: boolean,
    state: string,
  ): Promise<void>;
};

const summaryTabPage: SummaryTabPage = {
  summaryTab: `.mat-tab-label-content:text-is("Summary")`,

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    representationPresent: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      expect(page.locator("dl[id='case-details'] h3")).toHaveText(
        summaryTabContent.subHeading1,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const textOnPage = (summaryTabContent as any)[`textOnPage${index + 1}`];
        return expect(page.locator(".case-viewer-label").nth(index)).toHaveText(
          textOnPage,
        );
      }),
    ]);
    if (representationPresent) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(`h3:has-text("${summaryTabContent.subHeading2}")`),
        1,
      );
      await Promise.all([
        ...Array.from({ length: 5 }, (_, index) => {
          const textOnPage = (summaryTabContent as any)[
            `textOnPage${index + 6}`
          ];
          return expect(
            page.locator(".case-viewer-label").nth(index + 5),
          ).toHaveText(textOnPage);
        }),
      ]);
    }

    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async changeToSummaryTab(page: Page): Promise<void> {
    await page.locator(this.summaryTab).click();
  },

  async checkPageInfo(
    page: Page,
    caseNumber: string,
    representationPresent: boolean,
    representationQualified: boolean,
    subjectName: string,
    decisionDate?: Date,
  ): Promise<void> {
    const expectedInTimeValue = decisionDate ? "No" : "Yes";
    await Promise.all([
      expect(
        page.locator("td[id='case-viewer-field-read--cicCaseFullName']"),
      ).toHaveText(subjectName),
      expect(
        page.locator("ccd-read-date-field[class='ng-star-inserted']"),
      ).toHaveText(await commonHelpers.convertDate(true)),
      expect(
        page.locator("ccd-read-email-field[class='ng-star-inserted']").nth(0),
      ).toHaveText(subjectContactDetailsContent.emailAddress),
      expect(
        page.locator("ccd-read-text-field[class='ng-star-inserted']").nth(1),
      ).toHaveText(caseNumber),
      expect(
        page.locator("#case-viewer-field-read--cicCaseIsCaseInTime"),
      ).toHaveText(expectedInTimeValue),
    ]);
    if (representationPresent) {
      await Promise.all([
        expect(
          page.locator("ccd-read-text-field[class='ng-star-inserted']").nth(2),
        ).toHaveText(representativeDetailsContent.Organisation),
        expect(
          page.locator("ccd-read-text-field[class='ng-star-inserted']").nth(3),
        ).toHaveText(representativeDetailsContent.fullName),
        expect(
          page.locator("ccd-read-text-field[class='ng-star-inserted']").nth(4),
        ).toHaveText(representativeDetailsContent.contactNumber),
        expect(
          page.locator("ccd-read-email-field[class='ng-star-inserted']").nth(1),
        ).toHaveText(representativeDetailsContent.emailAddress),
      ]);
      if (representationQualified) {
        await expect(page.locator("ccd-read-yes-no-field").nth(1)).toHaveText(
          "Yes",
        );
      } else {
        await expect(page.locator("ccd-read-yes-no-field").nth(1)).toHaveText(
          "No",
        );
      }
    }
  },

  async checkStayDetails(
    page: Page,
    stayReason: keyof typeof createEditStaySubmit_content,
    optionalText: boolean,
    state: string,
  ): Promise<void> {
    const stayReasonText = createEditStaySubmit_content[stayReason];
    await page.waitForSelector(
      `.text-16:has-text("${summaryTab_content.textOnPage10}")`,
    );
    await expect(page.locator("markdown.markdown > h4")).toContainText(
      summaryTab_content.caseState + state,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th#case-viewer-field-label > div.text-16:has-text("${summaryTab_content.textOnPage10}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:has-text("${summaryTab_content.textOnPage11}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-radio-list-field > span.text-16:has-text("${stayReasonText}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-date-field > span.text-16:has-text("${addStay_content.day} ${await commonHelpers.shortMonths(parseInt(addStay_content.month))} ${addStay_content.year}")`,
        ),
        1,
      ),
    ]);
    if (stayReason === "Other") {
      await expect(
        page.locator(
          `th#case-viewer-field-label > .text-16:has-text("${summaryTab_content.textOnPage13}")`,
        ),
      ).toBeVisible();
      await expect(
        page.locator(
          `ccd-read-text-field > span:has-text("${addStay_content.otherText}")`,
        ),
      ).toBeVisible();
    }
    if (optionalText) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th#case-viewer-field-label > div.text-16:has-text("${summaryTab_content.textOnPage12}")`,
          ),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `ccd-read-text-area-field > span:has-text("${addStay_content.optionalText}")`,
          ),
          1,
        ),
      ]);
    }
  },

  async checkRemoveStayDetails(
    page: Page,
    removeReason: keyof typeof removeStaySubmit_content,
    optionalText: boolean,
    state: string,
  ): Promise<void> {
    await page.waitForSelector(
      `th#case-viewer-field-label > div.text-16:has-text("${summaryTab_content.textOnPage15}")`,
    );
    await expect(page.locator("markdown.markdown > h4")).toContainText(
      summaryTab_content.caseState + state,
    );
    await expect(
      page.locator(
        `th#case-viewer-field-label > div.text-16:has-text("${summaryTab_content.textOnPage15}")`,
      ),
    ).toBeVisible();

    if (removeReason === "Other") {
      await expect(
        page.locator(
          `th#case-viewer-field-label > div.text-16:has-text("${summaryTab_content.textOnPage16}")`,
        ),
      ).toBeVisible();
      await expect(
        page
          .locator("#case-viewer-field-read--removeStayStayRemoveReason")
          .getByText("Other"),
      ).toBeVisible();

      await expect(
        page.locator(
          `ccd-read-text-field > span:has-text("${removeStay_content.otherText}")`,
        ),
      ).toBeVisible();
    }

    if (optionalText) {
      await expect(
        page
          .getByRole("cell", { name: "Provide additional details" })
          .locator("div"),
      ).toBeVisible();
      await expect(
        page.locator(
          `ccd-read-text-area-field > span:has-text("${removeStay_content.optionalText}")`,
        ),
      ).toBeVisible();
    }
  },
};

export default summaryTabPage;
