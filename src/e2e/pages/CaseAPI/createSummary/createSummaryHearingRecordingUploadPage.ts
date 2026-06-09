import { expect, Page } from "@playwright/test";
import config from "../../../config.ts";
import createSummaryHearingRecordingUploadContent from "../../../fixtures/content/CaseAPI/createSummary/createSummaryHearingRecordingUpload_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateSummaryHearingRecordingUploadPage = {
  previous: string;
  continue: string;
  cancel: string;
  remove: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createSummaryHearingRecordingUploadPage: CreateSummaryHearingRecordingUploadPage =
  {
    previous: "button[name='Previous']",
    continue: '[type="submit"]',
    cancel: ".cancel",
    remove: "button[aria-label='Remove Upload file']",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForURL(
        `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/create-hearing-summary/create-hearing-summaryhearingRecordingUploadPage`,
        { timeout: 30_000 },
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          createSummaryHearingRecordingUploadContent.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          createSummaryHearingRecordingUploadContent.pageTitle,
        ),
        expect(page.locator("markdown > h3").nth(0)).toContainText(
          `${subjectName}`,
        ),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          createSummaryHearingRecordingUploadContent.caseReference + caseNumber,
        ),
        expect(page.locator("markdown > h2")).toContainText(
          createSummaryHearingRecordingUploadContent.title,
        ),
        expect(page.locator("markdown > p").nth(1)).toContainText(
          createSummaryHearingRecordingUploadContent.textOnPage1,
        ),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (
            createSummaryHearingRecordingUploadContent as any
          )[`textOnPage${index + 2}`];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#theHearingRecordingUpload > dt > ccd-markdown > div > markdown > ul > li > p:text-is("${textOnPage}")`,
            ),
            1,
          );
        }),
        expect(page.locator("markdown > p").nth(2)).toContainText(
          createSummaryHearingRecordingUploadContent.textOnPage5,
        ),
        expect(page.locator(".heading-h2")).toContainText(
          createSummaryHearingRecordingUploadContent.subTitle1,
        ),
        expect(page.locator("markdown > h3").nth(1)).toContainText(
          createSummaryHearingRecordingUploadContent.textOnPage10,
        ),
        expect(
          page.locator(
            "#caseEditForm > div > ccd-field-write > div > ccd-write-text-area-field > div > label > span",
          ),
        ).toHaveText(createSummaryHearingRecordingUploadContent.textOnPage11),
        commonHelpers.checkForButtons(
          page,
          this.continue,
          this.previous,
          this.cancel,
        ),
      ]);
      await page.getByRole("button", { name: "Add new" }).click();
      await Promise.all([
        expect(page.locator("label > h3")).toHaveText(
          createSummaryHearingRecordingUploadContent.textOnPage6,
        ),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (
            createSummaryHearingRecordingUploadContent as any
          )[`textOnPage${index + 7}`];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.form-label:text-is("${textOnPage}")`),
            1,
          );
        }),
      ]);
      await page.click(this.remove);
      await expect(page.locator(".cdk-overlay-container")).toBeVisible();
      await page.locator("button[title='Remove']").click();

      // if (accessibilityTest) {
      //   await new AxeUtils(page).audit();
      // }
    },

    async fillFields(page: Page): Promise<void> {
      await page.getByRole("button", { name: "Add new" }).click();
      await page
        .locator("#recFileUpload_0_documentCategory")
        .selectOption({ index: 1 });
      await page
        .locator("#recFileUpload_0_documentEmailContent")
        .fill(createSummaryHearingRecordingUploadContent.description);
      await page
        .locator("#recFileUpload_0_documentLink")
        .setInputFiles(config.testMP3File);
      await page.locator(".error-message").waitFor({ state: "hidden" });
      await page
        .locator("#recDesc")
        .fill(createSummaryHearingRecordingUploadContent.recordingLocation);
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default createSummaryHearingRecordingUploadPage;
