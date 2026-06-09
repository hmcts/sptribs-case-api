import { expect, Page } from "@playwright/test";
import path from "path";
import config from "../../../config.ts";
import editSummaryHearingRecordingUploadContent from "../../../fixtures/content/CaseAPI/editSummary/editSummaryHearingRecordingUpload_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditSummaryHearingRecordingUploadPage = {
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
  checkFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editSummaryHearingRecordingUploadPage: EditSummaryHearingRecordingUploadPage =
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
        `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/edit-hearing-summary/edit-hearing-summaryhearingRecordingUploadPage`,
        { timeout: 30_000 },
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          editSummaryHearingRecordingUploadContent.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          editSummaryHearingRecordingUploadContent.pageTitle,
        ),
        expect(page.locator("markdown > h3").nth(0)).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          editSummaryHearingRecordingUploadContent.caseReference + caseNumber,
        ),
        expect(page.locator("markdown > h2")).toContainText(
          editSummaryHearingRecordingUploadContent.title,
        ),
        expect(page.locator("markdown > p").nth(1)).toContainText(
          editSummaryHearingRecordingUploadContent.textOnPage1,
        ),
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (editSummaryHearingRecordingUploadContent as any)[
            `textOnPage${index + 2}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              `#theHearingRecordingUpload > dt > ccd-markdown > div > markdown > ul > li > p:text-is("${textOnPage}")`,
            ),
            1,
          );
        }),
        expect(page.locator("markdown > p").nth(2)).toContainText(
          editSummaryHearingRecordingUploadContent.textOnPage5,
        ),
        expect(page.locator("h2.error-spacing")).toContainText(
          editSummaryHearingRecordingUploadContent.subTitle1,
        ),
        expect(page.locator("markdown > h3").nth(1)).toContainText(
          editSummaryHearingRecordingUploadContent.textOnPage10,
        ),
        expect(
          page.locator(
            "#caseEditForm > div > ccd-field-write > div > ccd-write-text-area-field > div > label > span",
          ),
        ).toHaveText(editSummaryHearingRecordingUploadContent.textOnPage11),
        commonHelpers.checkForButtons(
          page,
          this.continue,
          this.previous,
          this.cancel,
        ),
      ]);

      // if (accessibilityTest) {
      //   await new AxeUtils(page).audit();
      // }
    },

    async checkFields(page: Page): Promise<void> {
      await expect(
        page.locator("#recFileUpload_0_documentCategory"),
      ).toHaveValue("1: ApplicationForm");
      await expect(
        page.locator("#recFileUpload_0_documentEmailContent"),
      ).toHaveValue(editSummaryHearingRecordingUploadContent.description);
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-document-field > button:text-is("${path.basename(config.testMP3File)}")`,
        ),
        1,
      );
      await expect(page.locator("#recDesc")).toHaveValue(
        editSummaryHearingRecordingUploadContent.recordingLocation,
      );
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default editSummaryHearingRecordingUploadPage;
