import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editSummaryHearingAttendeesRoleContent from "../../../fixtures/content/CaseAPI/editSummary/editSummaryHearingAttendeesRole_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditSummaryHearingAttendeesRolePage = {
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  checkFields(page: Page): Promise<void>;
  triggerErrorMessages(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editSummaryHearingAttendeesRolePage: EditSummaryHearingAttendeesRolePage =
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
      await page.waitForSelector(
        `.govuk-heading-l:text-is("${editSummaryHearingAttendeesRoleContent.pageTitle}")`,
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          editSummaryHearingAttendeesRoleContent.pageHint,
        ),
        expect(page.locator("markdown > h3")).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          editSummaryHearingAttendeesRoleContent.caseReference + caseNumber,
        ),
        expect(page.locator("#roles > fieldset > legend > span")).toHaveText(
          editSummaryHearingAttendeesRoleContent.textOnPage1,
        ),
        ...Array.from({ length: 17 }, (_, index) => {
          const textOnPage = (editSummaryHearingAttendeesRoleContent as any)[
            `textOnPage${index + 2}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#roles > fieldset > div > label.form-label:text-is("${textOnPage}")`,
            ),
            1,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `#caseEditForm > div > ccd-field-write > div > ccd-write-text-field > div > label > span:text-is("${editSummaryHearingAttendeesRoleContent.textOnPage19}")`,
          ),
          1,
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

    async checkFields(page: Page): Promise<void> {
      await Promise.all([
        ...Array.from({ length: 17 }, (_, index) => {
          const textOnPage = (editSummaryHearingAttendeesRoleContent as any)[
            `textOnPage${index + 2}`
          ];
          return expect(
            page.getByLabel(textOnPage, { exact: true }),
          ).toBeChecked();
        }),
        expect(page.locator("#others")).toHaveValue(
          editSummaryHearingAttendeesRoleContent.otherAttendee,
        ),
      ]);
    },

    async triggerErrorMessages(page: Page): Promise<void> {
      for (let i = 2; i < 19; i++) {
        const label = (editSummaryHearingAttendeesRoleContent as any)[
          `textOnPage${i}`
        ];
        await page.getByLabel(label, { exact: true }).uncheck();
      }
      await page.click(this.continue);
      await Promise.all([
        expect(page.locator("#error-summary-title")).toHaveText(
          editSummaryHearingAttendeesRoleContent.errorBanner,
        ),
        expect(page.locator(".error-message")).toHaveText(
          editSummaryHearingAttendeesRoleContent.attendanceError,
        ),
        expect(page.locator(".validation-error")).toHaveText(
          editSummaryHearingAttendeesRoleContent.attendanceError,
        ),
      ]);
      await page
        .getByLabel(editSummaryHearingAttendeesRoleContent.textOnPage18, {
          exact: true,
        })
        .click();
      await page.locator("#others").clear();
      await page.waitForTimeout(1000);
      await page.click(this.continue);
      await Promise.all([
        expect(page.locator("#error-summary-title")).toHaveText(
          editSummaryHearingAttendeesRoleContent.errorBanner,
        ),
        expect(page.locator(".error-message")).toHaveText(
          editSummaryHearingAttendeesRoleContent.otherAttendanceError,
        ),
        expect(page.locator(".validation-error")).toHaveText(
          editSummaryHearingAttendeesRoleContent.otherAttendanceError,
        ),
      ]);
      for (let i = 2; i < 18; i++) {
        const label = (editSummaryHearingAttendeesRoleContent as any)[
          `textOnPage${i}`
        ];
        await page.getByLabel(label, { exact: true }).check();
      }
      await page
        .locator("#others")
        .fill(editSummaryHearingAttendeesRoleContent.otherAttendee);
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default editSummaryHearingAttendeesRolePage;
