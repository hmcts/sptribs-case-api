import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseDetailsTabContent from "../../../fixtures/content/CaseAPI/caseTabs/caseDetailsTab_content.ts";
import representativeDetailsContent from "../../../fixtures/content/DSSCreateCase/RepresentativeDetails_content.ts";
import subjectContactDetailsContent from "../../../fixtures/content/DSSCreateCase/SubjectContactDetails_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CaseDetailsTabPage = {
  caseDetailsTab: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    representationPresent: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void>;
  changeToCaseDetailsTab(page: Page): Promise<void>;
  checkPageInfo(
    page: Page,
    representationPresent: boolean,
    representationQualified: boolean,
    subjectName: string,
  ): Promise<void>;
};

const caseDetailsTabPage: CaseDetailsTabPage = {
  caseDetailsTab: `.mat-tab-label-content:text-is("Case Details")`,

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    representationPresent: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<void> {
    await Promise.all([
      commonHelpers.checkAllCaseTabs(page, caseNumber, false, subjectName),
      expect(page.locator("dl[id='case-details'] h3")).toHaveText(
        caseDetailsTabContent.pageTitle,
      ),
      expect(page.locator(".case-viewer-label").nth(0)).toHaveText(
        caseDetailsTabContent.textOnPage1,
      ),
      expect(page.locator("dl[id='objectSubjects'] h3")).toHaveText(
        caseDetailsTabContent.subHeading1,
      ),
      ...Array.from({ length: 5 }, (_, index) => {
        const textOnPage = (caseDetailsTabContent as any)[
          `textOnPage${index + 2}`
        ];
        return expect(
          page.locator(".case-viewer-label").nth(index + 1),
        ).toHaveText(textOnPage);
      }),
    ]);
    if (representationPresent) {
      await Promise.all([
        expect(page.locator("dl[id='applicantDetails'] h3")).toHaveText(
          caseDetailsTabContent.subHeading2,
        ),
        ...Array.from({ length: 7 }, (_, index) => {
          const textOnPage = (caseDetailsTabContent as any)[
            `textOnPage${index + 6}`
          ];
          return expect(
            page.locator(".case-viewer-label").nth(index + 6),
          ).toHaveText(textOnPage);
        }),
      ]);
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async changeToCaseDetailsTab(page: Page): Promise<void> {
    await page.locator(this.caseDetailsTab).click();
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
        page.locator("td[id='case-viewer-field-read--cicCaseDateOfBirth']"),
      ).toHaveText(await commonHelpers.convertDate(true)),
      expect(
        page.locator("ccd-read-email-field[class='ng-star-inserted']").nth(0),
      ).toHaveText(subjectContactDetailsContent.emailAddress),
      expect(
        page.locator("td[id='case-viewer-field-read--cicCasePhoneNumber']"),
      ).toHaveText(subjectContactDetailsContent.contactNumber),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span.text-16:text-is("Subject")`),
        1,
      ),
    ]);
    if (representationPresent) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span.text-16:text-is("Representative")`),
          1,
        ),
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
          page.locator("ccd-read-email-field[class='ng-star-inserted']").nth(1),
        ).toHaveText(representativeDetailsContent.emailAddress),
        expect(
          page.locator(
            "ccd-read-fixed-radio-list-field[class='ng-star-inserted']",
          ),
        ).toHaveText("Email"),
      ]);
      if (representationQualified) {
        await expect(page.locator("ccd-read-yes-no-field")).toHaveText("Yes");
      } else {
        await expect(page.locator("ccd-read-yes-no-field")).toHaveText("No");
      }
    }
  },
};

export default caseDetailsTabPage;
