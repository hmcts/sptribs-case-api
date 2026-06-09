import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import createListingRemoteHearingInformationContent from "../../../fixtures/content/CaseAPI/createListing/createListingRemoteHearingInformation_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CreateListingRemoteHearingInformationPage = {
  videoLink: string;
  conferenceNumber: string;
  previous: string;
  continue: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    subjectName: string,
  ): Promise<void>;
  fillInFields(page: Page): Promise<void>;
  continueOn(page: Page): Promise<void>;
};

const createListingRemoteHearingInformationPage: CreateListingRemoteHearingInformationPage =
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
        `**/case-details/ST_CIC/CriminalInjuriesCompensation/${caseNumber.replace(/-/g, "")}/trigger/caseworker-record-listing/caseworker-record-listingremoteHearingInformation`,
        { timeout: 30_000 },
      );
      await Promise.all([
        expect(page.locator(".govuk-caption-l")).toHaveText(
          createListingRemoteHearingInformationContent.pageHint,
        ),
        expect(page.locator(".govuk-heading-l")).toHaveText(
          createListingRemoteHearingInformationContent.pageTitle,
        ),
        expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
        expect(page.locator("markdown > p").nth(0)).toContainText(
          createListingRemoteHearingInformationContent.caseReference +
            caseNumber,
        ),
        ...Array.from({ length: 2 }, (_, index) => {
          const textOnPage = (
            createListingRemoteHearingInformationContent as any
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

    async fillInFields(page: Page): Promise<void> {
      await page.fill(
        this.videoLink,
        createListingRemoteHearingInformationContent.videoCallLink,
      );
      await page.fill(
        this.conferenceNumber,
        createListingRemoteHearingInformationContent.conferenceCallNumber,
      );
    },

    async continueOn(page: Page): Promise<void> {
      await page.click(this.continue);
    },
  };

export default createListingRemoteHearingInformationPage;
