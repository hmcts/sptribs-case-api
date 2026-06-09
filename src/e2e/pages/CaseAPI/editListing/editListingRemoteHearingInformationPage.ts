import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import editListingRemoteHearingInformationContent from "../../../fixtures/content/CaseAPI/editListing/editListingRemoteHearingInformation_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type EditListingRemoteHearingInformationPage = {
  videoLink: string;
  conferenceNumber: string;
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectname: string,
  ): Promise<void>;
  checkFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const editListingRemoteHearingInformationPage: EditListingRemoteHearingInformationPage =
  {
    videoLink: "#videoCallLink",
    conferenceNumber: "#conferenceCallNumber",
    previous: ".button-secondary",
    continue: '[type="submit"]',
    cancel: ".cancel",

    async checkPageLoads(
      page: Page,
      caseNumber: string,
      accessibilityTest: boolean,
      subjectName: string,
    ): Promise<void> {
      await page.waitForURL(
        `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/caseworker-edit-record-listing/caseworker-edit-record-listingremoteHearingInformation`,
        { timeout: 30_000 },
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          editListingRemoteHearingInformationContent.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          editListingRemoteHearingInformationContent.pageTitle,
        ),
        expect(page.locator("markdown > h3")).toContainText(subjectName),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          editListingRemoteHearingInformationContent.caseReference + caseNumber,
        ),
        ...Array.from({ length: 2 }, (_, index) => {
          const textOnPage = (
            editListingRemoteHearingInformationContent as any
          )[`textOnPage${index + 1}`];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.form-label:text-is("${textOnPage}")`),
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
      if (accessibilityTest) {
        await new AxeUtils(page).audit();
      }
    },

    async checkFields(page: Page): Promise<void> {
      await expect(page.locator(this.videoLink)).toHaveValue(
        editListingRemoteHearingInformationContent.videoCallLink,
      );
      await expect(page.locator(this.conferenceNumber)).toHaveValue(
        editListingRemoteHearingInformationContent.conferenceCallNumber,
      );
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default editListingRemoteHearingInformationPage;
