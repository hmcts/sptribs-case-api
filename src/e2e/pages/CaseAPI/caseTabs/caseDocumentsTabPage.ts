import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import path from "path";
import config, { UserRole } from "../../../config.ts";
import caseDocumentsTabContent from "../../../fixtures/content/CaseAPI/caseTabs/caseDocumentsTab_content.ts";
import commonHelpers from "../../../helpers/commonHelpers.ts";

type CaseDocumentsTabPage = {
  caseDocumentsTab: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    multipleDocuments: boolean,
    uploadOtherInfo: boolean,
    docManagementUploadJourney: boolean,
    user: UserRole,
    subjectName: string,
  ): Promise<void>;
  changeToCaseDocumentsTab(page: Page): Promise<void>;
  checkPageInfo(
    page: Page,
    multipleDocuments: boolean,
    uploadOtherInfo: boolean,
  ): Promise<void>;
  handleMultipleDocumentsLoad(
    page: Page,
    uploadOtherInfo: boolean,
  ): Promise<void>;
  handleMultipleDocuments(page: Page, uploadOtherInfo: boolean): Promise<void>;
  handleTodayDate(): Promise<string>;
  docManagementUploadCheckInfo(
    page: Page,
    multipleDocuments: boolean,
    user: UserRole,
    category: string,
    message: string,
    documentManagementAmendJourney: boolean,
  ): Promise<void>;
};

const caseDocumentsTabPage: CaseDocumentsTabPage = {
  caseDocumentsTab: `.mat-tab-label-content:text-is("Case Documents")`,

  async checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    caseNumber: string,
    multipleDocuments: boolean,
    uploadOtherInfo: boolean,
    docManagementUploadJourney: boolean,
    user: UserRole,
    subjectName: string,
  ): Promise<void> {
    if (user === "respondent") {
      await commonHelpers.checkAllCaseTabs(page, caseNumber, true, subjectName);
    } else {
      await commonHelpers.checkAllCaseTabs(
        page,
        caseNumber,
        false,
        subjectName,
      );
    }
    await Promise.all([
      expect(page.locator("markdown[class='markdown'] h4")).toHaveText(
        caseDocumentsTabContent.pageTitle,
      ),
      expect(page.locator(".text-16").nth(1)).toHaveText(
        caseDocumentsTabContent.subHeading1,
      ),
    ]);
    if (docManagementUploadJourney) {
      await Promise.all([
        expect(page.locator(".case-viewer-label").nth(1)).toHaveText(
          caseDocumentsTabContent.subHeading2,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${caseDocumentsTabContent.title1}")`),
          1,
        ),
      ]);
      if (!multipleDocuments) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.docTitle1}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage1}")`,
            ),
            2,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage2}")`,
            ),
            2,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage4}")`,
            ),
            2,
          ),
        ]);
      } else {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.docTitle1}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.docTitle2}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.docTitle3}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage1}")`,
            ),
            4,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage2}")`,
            ),
            4,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage4}")`,
            ),
            4,
          ),
        ]);
      }
    } else {
      if (!uploadOtherInfo) {
        if (!multipleDocuments) {
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.title1}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.title2}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.textOnPage1}")`,
              ),
              2,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.textOnPage2}")`,
              ),
              2,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.textOnPage3}")`,
              ),
              2,
            ),
          ]);
        } else {
          await this.handleMultipleDocumentsLoad(page, uploadOtherInfo);
        }
      } else {
        if (!multipleDocuments) {
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.title1}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.title2}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.title3}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.textOnPage1}")`,
              ),
              3,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.textOnPage2}")`,
              ),
              3,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.text-16:text-is("${caseDocumentsTabContent.textOnPage3}")`,
              ),
              3,
            ),
          ]);
        } else {
          await this.handleMultipleDocumentsLoad(page, uploadOtherInfo);
        }
      }
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async changeToCaseDocumentsTab(page: Page): Promise<void> {
    await page.locator(this.caseDocumentsTab).click();
  },

  async checkPageInfo(
    page: Page,
    multipleDocuments: boolean,
    uploadOtherInfo: boolean,
  ): Promise<void> {
    if (!uploadOtherInfo) {
      if (!multipleDocuments) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.secondDocCategory}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`button:text-is("${path.basename(config.testFile)}")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.thirdDocCategory}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `button:text-is("${path.basename(config.testPdfFile)}")`,
            ),
            1,
          ),

          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${await this.handleTodayDate()}")`),
            2,
          ),
        ]);
      } else {
        await this.handleMultipleDocuments(page, uploadOtherInfo);
      }
    } else {
      if (!multipleDocuments) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.firstDocCategory}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `button:text-is("${path.basename(config.testWordFile)}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.secondDocCategory}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`button:text-is("${path.basename(config.testFile)}")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.thirdDocCategory}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `button:text-is("${path.basename(config.testPdfFile)}")`,
            ),
            1,
          ),

          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${await this.handleTodayDate()}")`),
            3,
          ),
        ]);
      } else {
        await this.handleMultipleDocuments(page, uploadOtherInfo);
      }
    }
  },

  async handleMultipleDocumentsLoad(
    page: Page,
    uploadOtherInfo: boolean,
  ): Promise<void> {
    if (!uploadOtherInfo) {
      let count = 8;
      await Promise.all([
        ...Array.from({ length: count }, (_, index) => {
          const textOnPage = (caseDocumentsTabContent as any)[
            `title${index + 1}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.textOnPage1}")`,
          ),
          count,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.textOnPage2}")`,
          ),
          count,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.textOnPage3}")`,
          ),
          count,
        ),
      ]);
    } else {
      let count = 12;
      await Promise.all([
        ...Array.from({ length: count }, (_, index) => {
          const textOnPage = (caseDocumentsTabContent as any)[
            `title${index + 1}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${textOnPage}")`),
            1,
          );
        }),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.textOnPage1}")`,
          ),
          count,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.textOnPage2}")`,
          ),
          count,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.textOnPage3}")`,
          ),
          count,
        ),
      ]);
    }
  },

  async handleMultipleDocuments(
    page: Page,
    uploadOtherInfo: boolean,
  ): Promise<void> {
    if (!uploadOtherInfo) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.secondDocCategory}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`button:text-is("${path.basename(config.testFile)}")`),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.thirdDocCategory}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `button:text-is("${path.basename(config.testPdfFile)}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span:text-is("${await this.handleTodayDate()}")`),
          8,
        ),
      ]);
    } else {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.firstDocCategory}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `button:text-is("${path.basename(config.testWordFile)}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.secondDocCategory}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`button:text-is("${path.basename(config.testFile)}")`),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `.text-16:text-is("${caseDocumentsTabContent.thirdDocCategory}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(
            `button:text-is("${path.basename(config.testPdfFile)}")`,
          ),
          4,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span:text-is("${await this.handleTodayDate()}")`),
          12,
        ),
      ]);
    }
  },

  async handleTodayDate(): Promise<string> {
    const now = new Date();
    const dateString = now.toLocaleDateString("en-US", {
      year: "numeric",
      month: "2-digit",
      day: "numeric",
    });
    const [month, day, year] = dateString.split("/");
    return `${day} ${await commonHelpers.shortMonths(parseInt(month))} ${year}`;
  },

  async docManagementUploadCheckInfo(
    page: Page,
    multipleDocuments: boolean,
    user: UserRole,
    category: string,
    message: string,
    documentManagementAmendJourney: boolean,
  ): Promise<void> {
    await commonHelpers.checkVisibleAndPresent(
      page.locator(`button:text-is("${path.basename(config.testPdfFile)}")`),
      2,
    );
    if (!(user === "respondent")) {
      if (multipleDocuments) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage3}")`,
            ),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${await this.handleTodayDate()}")`),
            3,
          ),
        ]);
      } else {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `.text-16:text-is("${caseDocumentsTabContent.textOnPage3}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${await this.handleTodayDate()}")`),
            1,
          ),
        ]);
      }
    }
    if (!multipleDocuments) {
      await Promise.all([
        commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${category}")`).nth(0),
          1,
        ),
        commonHelpers.checkVisibleAndPresent(
          page.locator(`span:text-is("${message}")`).nth(0),
          1,
        ),
      ]);
    } else {
      if (!documentManagementAmendJourney) {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${category}")`),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${message}")`),
            3,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `button:text-is("${path.basename(config.testWordFile)}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`button:text-is("${path.basename(config.testFile)}")`),
            1,
          ),
        ]);
      } else {
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(`.text-16:text-is("${category}")`),
            2,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`span:text-is("${message}")`),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `button:text-is("${path.basename(config.testWordFile)}")`,
            ),
            1,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(`button:text-is("${path.basename(config.testFile)}")`),
            1,
          ),
        ]);
      }
    }
  },
};

export default caseDocumentsTabPage;
