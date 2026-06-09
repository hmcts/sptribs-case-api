import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import allTabTitlesContent from "../../../fixtures/content/CaseAPI/caseTabs/allTabTitles_content.ts";
import casePartiesTabContent from "../../../fixtures/content/CaseAPI/caseTabs/casePartiesTab_content.ts";
import representativeDetailsContent from "../../../fixtures/content/DSSCreateCase/RepresentativeDetails_content.ts";
import subjectContactDetailsContent from "../../../fixtures/content/DSSCreateCase/SubjectContactDetails_content.ts";
import respondentDetailsContent from "../../../fixtures/content/RespondentDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CasePartiesTabPage = {
  casePartiesTab: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    representationPresent: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  changeToCasePartiesTab(page: Page): Promise<void>;
  checkPageInfo(
    page: Page,
    representationPresent: boolean,
    representationQualified: boolean,
    subjectName: string,
  ): Promise<void>;
};

const casePartiesTabPage: CasePartiesTabPage = {
  casePartiesTab: `.mat-tab-label-content:text-is("Case Parties")`,

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    representationPresent: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      commonHelpers.checkAllCaseTabs(page, caseNumber, false, subjectName),
      expect(page.locator(".case-field").nth(1)).toHaveText(
        casePartiesTabContent.subHeading1,
      ),
      ...Array.from({ length: 5 }, (_, index) => {
        const textOnPage = (casePartiesTabContent as any)[
          `textOnPage${index + 1}`
        ];
        return expect(page.locator(".case-viewer-label").nth(index)).toHaveText(
          textOnPage,
        );
      }),
      expect(page.locator(".case-field").nth(4)).toHaveText(
        casePartiesTabContent.subHeading3,
      ),
    ]);
    if (representationPresent) {
      await Promise.all([
        expect(page.locator(".case-field").nth(3)).toHaveText(
          casePartiesTabContent.subHeading2,
        ),
        ...Array.from({ length: 9 }, (_, index) => {
          const textOnPage = (casePartiesTabContent as any)[
            `textOnPage${index + 6}`
          ];
          return expect(
            page.locator(".case-viewer-label").nth(index + 5),
          ).toHaveText(textOnPage);
        }),
      ]);
    } else {
      await Promise.all([
        ...Array.from({ length: 3 }, (_, index) => {
          const textOnPage = (casePartiesTabContent as any)[
            `textOnPage${index + 12}`
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

  async changeToCasePartiesTab(page: Page): Promise<void> {
    await page.locator(this.casePartiesTab).click();
  },

  async checkPageInfo(
    page: Page,
    representationPresent: boolean,
    representationQualified: boolean,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      expect(
        page.locator("td[id='case-viewer-field-read--cicCaseFullName']"),
      ).toHaveText(subjectName),
      expect(
        page.locator("ccd-read-email-field[class='ng-star-inserted']").nth(0),
      ).toHaveText(subjectContactDetailsContent.emailAddress),
      expect(
        page.locator("td[id='case-viewer-field-read--cicCasePhoneNumber']"),
      ).toHaveText(subjectContactDetailsContent.contactNumber),
      expect(
        page.locator("td[id='case-viewer-field-read--cicCaseDateOfBirth']"),
      ).toHaveText(await commonHelpers.convertDate(true)),
      expect(
        page
          .locator("ccd-read-fixed-radio-list-field[class='ng-star-inserted']")
          .first(),
      ).toHaveText(allTabTitlesContent.contactPreference),
      expect(
        page.locator("td[id='case-viewer-field-read--cicCaseRespondentName']"),
      ).toHaveText(respondentDetailsContent.fullName),
      expect(
        page.locator(
          "td[id='case-viewer-field-read--cicCaseRespondentOrganisation']",
        ),
      ).toHaveText(respondentDetailsContent.Organisation),
      expect(
        page.locator("td[id='case-viewer-field-read--cicCaseRespondentEmail']"),
      ).toHaveText(respondentDetailsContent.emailAddress),
    ]);
    if (representationPresent) {
      await Promise.all([
        expect(
          page.locator(
            "td[id='case-viewer-field-read--cicCaseRepresentativeFullName']",
          ),
        ).toHaveText(representativeDetailsContent.fullName),
        expect(
          page.locator(
            "td[id='case-viewer-field-read--cicCaseRepresentativeOrgName']",
          ),
        ).toHaveText(representativeDetailsContent.Organisation),
        expect(
          page.locator(
            "td[id='case-viewer-field-read--cicCaseRepresentativePhoneNumber']",
          ),
        ).toHaveText(representativeDetailsContent.contactNumber),
        expect(
          page.locator(
            "td[id='case-viewer-field-read--cicCaseRepresentativeEmailAddress']",
          ),
        ).toHaveText(representativeDetailsContent.emailAddress),
        expect(
          page.locator(
            "td[id='case-viewer-field-read--cicCaseRepresentativeContactDetailsPreference']",
          ),
        ).toHaveText(allTabTitlesContent.contactPreference),
      ]);
      if (representationQualified) {
        await expect(page.locator("ccd-read-yes-no-field")).toHaveText("Yes");
      } else {
        await expect(page.locator("ccd-read-yes-no-field")).toHaveText("No");
      }
    }
  },
};

export default casePartiesTabPage;
