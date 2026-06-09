import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createSummaryHearingAttendeesRoleContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingAttendeesRole_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateSummaryHearingAttendeesRolePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createSummaryHearingAttendeesRolePage: CreateSummaryHearingAttendeesRolePage =
  {
    previous: "button[name='Previous']",
    continue: '[type="submit"]',
    cancel: ".cancel",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForURL(
        `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/create-hearing-summary/create-hearing-summaryhearingAttendeesRole`,
        { timeout: 30_000 },
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          createSummaryHearingAttendeesRoleContent.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          createSummaryHearingAttendeesRoleContent.pageTitle,
        ),
        expect(page.locator("markdown > h3")).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          createSummaryHearingAttendeesRoleContent.caseReference + caseNumber,
        ),
        expect(page.locator("#roles > fieldset > legend > span")).toHaveText(
          createSummaryHearingAttendeesRoleContent.textOnPage1,
        ),
        ...Array.from({ length: 17 }, (_, index) => {
          const textOnPage = (createSummaryHearingAttendeesRoleContent as any)[
            `textOnPage${index + 2}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#roles > fieldset > div > label.form-label:text-is("${textOnPage}")`,
            ),
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
      await page
        .getByLabel(createSummaryHearingAttendeesRoleContent.textOnPage18, {
          exact: true,
        })
        .check();
      await page.waitForTimeout(1000);
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `#caseEditForm > div > ccd-field-write > div > ccd-write-text-field > div > label > span:text-is("${createSummaryHearingAttendeesRoleContent.textOnPage19}")`,
        ),
        1,
      );
      await page
        .getByLabel(createSummaryHearingAttendeesRoleContent.textOnPage18, {
          exact: true,
        })
        .click();

      if (accessibilityTest) {
        await new AxeUtils(page).audit();
      }
    },

    async fillFields(page: Page): Promise<void> {
      for (let i = 2; i < 19; i++) {
        const label = (createSummaryHearingAttendeesRoleContent as any)[
          `textOnPage${i}`
        ];
        await page.getByLabel(label, { exact: true }).click();
      }
      await page
        .locator("#others")
        .fill(createSummaryHearingAttendeesRoleContent.otherAttendee);
    },

    async triggerErrorMessages(page: Page): Promise<void> {
      await page.click(this.continue);
      await Promise.all([
        expect(page.locator("#error-summary-title")).toHaveText(
          createSummaryHearingAttendeesRoleContent.errorBanner,
        ),
        expect(page.locator(".error-message")).toHaveText(
          createSummaryHearingAttendeesRoleContent.attendanceError,
        ),
      ]);
      await page
        .getByLabel(createSummaryHearingAttendeesRoleContent.textOnPage18, {
          exact: true,
        })
        .click();
      await page.waitForTimeout(1000);
      await page.click(this.continue);
      await Promise.all([
        expect(page.locator("#error-summary-title")).toHaveText(
          createSummaryHearingAttendeesRoleContent.errorBanner,
        ),
        expect(page.locator(".error-message")).toHaveText(
          createSummaryHearingAttendeesRoleContent.otherAttendanceError,
        ),
      ]);
      await page
        .getByLabel(createSummaryHearingAttendeesRoleContent.textOnPage18, {
          exact: true,
        })
        .click();
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default createSummaryHearingAttendeesRolePage;
