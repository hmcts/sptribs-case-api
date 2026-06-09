import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import caseObjectContacts_content from "../../../fixtures/content/CaseAPI/createCase/caseObjectContacts_content.ts";
import editCaseApplicantDetailsObject_content from "../../../fixtures/content/CaseAPI/editCase/editCaseApplicantDetailsObject_content.ts";
import editCaseObjectsContacts_content from "../../../fixtures/content/CaseAPI/editCase/editCaseObjectsContacts_content.ts";
import editCaseRepresentativeDetailsObject_content from "../../../fixtures/content/CaseAPI/editCase/editCaseRepresentativeDetailsObject_content.ts";
import editCaseSubjectDetailsObject_content from "../../../fixtures/content/CaseAPI/editCase/editCaseSubjectDetailsObject_content.ts";
import submit_content from "../../../fixtures/content/CaseAPI/editCase/submit_content.ts";
import commonHelpers, {
  caseRegion,
  Category,
  ContactPreference,
  Scheme,
  SubCategory,
} from "../../../helpers/commonHelpers.ts";

type SubmitPage = {
  saveAndContinue: string;
  previous: string;
  cancel: string;
  checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    contactPreference: ContactPreference,
    applicant: boolean,
    representative: boolean,
    tribunalFormsInTime: boolean,
    subjectName: string,
  ): Promise<void>;
  handleStandardLabels(
    page: Page,
    caseNumber: string,
    tribunalFormsInTime: boolean,
    subjectName: string,
  ): Promise<void>;
  handleContactLabels(
    page: Page,
    applicant: boolean,
    representative: boolean,
    contactPreference: ContactPreference,
  ): Promise<void>;
  handleApplicantLabels(page: Page): Promise<void>;
  handleRepresentativeLabels(page: Page): Promise<void>;
  checkValidInfo(
    page: Page,
    contactPreference: ContactPreference,
    applicant: boolean,
    representative: boolean,
    category: Category,
    subCategory: SubCategory,
    scheme: Scheme,
    caseRegion: caseRegion,
    representativeQualified: boolean,
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
    subjectName: string,
  ): Promise<void>;
  handleStandardInfo(
    page: Page,
    category: Category,
    subCategory: SubCategory,
    scheme: Scheme,
    caseRegion: caseRegion,
    values: number[],
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
    subjectName: string,
  ): Promise<void>;
  handleContactInfo(
    page: Page,
    applicant: boolean,
    representative: boolean,
    contactPreference: ContactPreference,
  ): Promise<void>;
  handleApplicantInfo(page: Page): Promise<void>;
  handleRepresentativeInfo(
    page: Page,
    representativeQualified: boolean,
  ): Promise<number[]>;
};

const submitPage: SubmitPage = {
  saveAndContinue: '[type="submit"]',
  previous: ".button-secondary",
  cancel: ".cancel",

  async checkPageLoads(
    page: Page,
    caseNumber: string,
    accessibilityTest: boolean,
    contactPreference: ContactPreference,
    applicant: boolean,
    representative: boolean,
    tribunalFormsInTime: boolean,
    subjectName: string,
  ): Promise<void> {
    await this.handleStandardLabels(
      page,
      caseNumber,
      tribunalFormsInTime,
      subjectName,
    );
    await this.handleContactLabels(
      page,
      applicant,
      representative,
      contactPreference,
    );
    if (applicant) {
      await this.handleApplicantLabels(page);
    }
    if (representative) {
      await this.handleRepresentativeLabels(page);
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async handleStandardLabels(
    page: Page,
    caseNumber: string,
    tribunalFormsInTime: boolean,
    subjectName: string,
  ): Promise<void> {
    await page.waitForSelector(
      `.govuk-heading-l:text-is("${submit_content.title}")`,
    );
    await Promise.all([
      expect(page.locator(".heading-h2")).toHaveText(submit_content.subTitle1),
      expect(page.locator("markdown > h3")).toContainText(`${subjectName}`),
      expect(page.locator("markdown > p").nth(0)).toContainText(
        submit_content.caseReference + caseNumber,
      ),
      commonHelpers.checkForButtons(
        page,
        this.saveAndContinue,
        this.previous,
        this.cancel,
      ),
      expect(page.locator(".text-16").nth(0)).toHaveText(
        submit_content.textOnPage1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage2}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage3}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage4}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage5}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage6}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage7}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage8}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage9}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage10}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage46}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage47}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage48}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage49}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage50}")`,
        ),
        1,
      ),
    ]);
    if (!tribunalFormsInTime) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage51}")`,
        ),
        1,
      );
    }
  },

  async handleContactLabels(
    page: Page,
    applicant: boolean,
    representative: boolean,
    contactPreference: ContactPreference,
  ): Promise<void> {
    switch (contactPreference) {
      case "Post":
        let count = 1;
        if (applicant && representative) {
          count = 3;
          // === Checking for subject's address field ===
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage22}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage22}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage33}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage33}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage11}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage12}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage13}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage14}")`,
              ),
              count,
            ),
          ]);
        } else if (applicant && !representative) {
          count = 2;
          // === Checking for subject's address field ===
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage22}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage22}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage11}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage12}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage13}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage14}")`,
              ),
              count,
            ),
          ]);
        } else if (representative && !applicant) {
          count = 2;
          // === Checking for subject's address field ===
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage33}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage33}")`,
              ),
              1,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage11}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage12}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage13}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage14}")`,
              ),
              count,
            ),
          ]);
        } else {
          // === Checking for subject's address field ===
          await Promise.all([
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `.complex-panel-title > dt > span.text-16:text-is("${submit_content.textOnPage10}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage11}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage12}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage13}")`,
              ),
              count,
            ),
            commonHelpers.checkVisibleAndPresent(
              page.locator(
                `th#complex-panel-simple-field-label > span.text-16:text-is("${submit_content.textOnPage14}")`,
              ),
              count,
            ),
          ]);
        }
        break;
      case "Email":
        await commonHelpers.checkVisibleAndPresent(
          page.locator(
            `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage16}")`,
          ),
          1,
        );
        if (applicant) {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage21}")`,
            ),
            1,
          );
        }
        if (representative) {
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage32}")`,
            ),
            1,
          );
        }
        break;
      default:
        console.log("You have not selected a valid contact type.");
        process.exit(1);
    }
  },

  async handleApplicantLabels(page: Page): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage17}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage18}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage19}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage20}")`,
        ),
        1,
      ),
    ]);
  },

  async handleRepresentativeLabels(page: Page): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage27}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage28}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage29}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage30}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `th.case-field-label > span.text-16:text-is("${submit_content.textOnPage31}")`,
        ),
        1,
      ),
    ]);
  },

  async checkValidInfo(
    page: Page,
    contactPreference: ContactPreference,
    applicant: boolean,
    representative: boolean,
    category: Category,
    subCategory: SubCategory,
    scheme: Scheme,
    caseRegion: caseRegion,
    representativeQualified: boolean,
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
    subjectName: string,
  ): Promise<void> {
    let values = [0, 0]; // Number of [Yes, No] values on a page
    if (representative) {
      // Must come first to add to the yes no counter before validation.
      values = await this.handleRepresentativeInfo(
        page,
        representativeQualified,
      );
    }
    await this.handleStandardInfo(
      page,
      category,
      subCategory,
      scheme,
      caseRegion,
      values,
      claimsLinked,
      compensationLinked,
      tribunalFormsInTime,
      applicantExplained,
      subjectName,
    );
    await this.handleContactLabels(
      page,
      applicant,
      representative,
      contactPreference,
    );
    if (applicant) {
      await this.handleApplicantInfo(page);
    }
    await page.click(this.saveAndContinue);
  },

  async handleStandardInfo(
    page: Page,
    category: Category,
    subCategory: SubCategory,
    scheme: Scheme,
    caseRegion: caseRegion,
    values: number[],
    claimsLinked: boolean,
    compensationLinked: boolean,
    tribunalFormsInTime: boolean,
    applicantExplained: boolean,
    subjectName: string,
  ): Promise<void> {
    const currentDate = new Date();
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-list-field > span.text-16:text-is("${category}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-fixed-list-field > span.text-16:text-is("${subCategory}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-read-date-field > span.text-16:text-is("${currentDate.getDate()} ${commonHelpers.months[currentDate.getMonth()].slice(0, 3)} ${currentDate.getFullYear()}")`,
        ),
        1,
      ),
    ]);
    if (!(subCategory === "Fatal" || subCategory === "Minor")) {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `tbody > tr > td > span.text-16:text-is("${editCaseObjectsContacts_content.textOnPage2}")`,
        ),
        2,
      );
    } else {
      await commonHelpers.checkVisibleAndPresent(
        page.locator(
          `tbody > tr > td > span.text-16:text-is("${editCaseObjectsContacts_content.textOnPage2}")`,
        ),
        1,
      );
    }
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${subjectName}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${editCaseSubjectDetailsObject_content.contactNumber}")`,
        ),
        1,
      ),
      expect(
        page
          .locator(
            `ccd-read-date-field > span.text-16:text-is("${editCaseSubjectDetailsObject_content.dayOfBirth} ${await commonHelpers.shortMonths(parseInt(editCaseSubjectDetailsObject_content.monthOfBirth))} ${editCaseSubjectDetailsObject_content.yearOfBirth}")`,
          )
          .first(),
      ).toBeVisible(),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-fixed-list-field > span.text-16:text-is("${scheme}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-fixed-list-field > span.text-16:text-is("${caseRegion}")`,
        ),
        1,
      ),
    ]);

    if (claimsLinked) {
      values[0]++;
    } else {
      values[1]++;
    }
    if (compensationLinked) {
      values[0]++;
    } else {
      values[1]++;
    }
    if (tribunalFormsInTime) {
      values[0]++;
    } else {
      values[1]++;
      if (applicantExplained) {
        values[0]++;
      } else {
        values[1]++;
      }
    }
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-yes-no-field > span.text-16:text-is("Yes")`,
        ),
        values[0],
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-yes-no-field > span.text-16:text-is("No")`,
        ),
        values[1],
      ),
    ]);
  },
  async handleContactInfo(
    page: Page,
    applicant: boolean,
    representative: boolean,
    contactPreference: ContactPreference,
  ): Promise<void> {
    switch (contactPreference) {
      case "Post":
        let count = 1;
        if (applicant && representative) {
          count = 3;
        } else if (
          (applicant && !representative) ||
          (representative && !applicant)
        ) {
          count = 2;
        }
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${caseObjectContacts_content.buildingAndStreet}")`,
            ),
            count,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${caseObjectContacts_content.townOrCity}")`,
            ),
            count,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${caseObjectContacts_content.country}")`,
            ),
            count,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${caseObjectContacts_content.postCode}")`,
            ),
            count,
          ),
        ]);
        break;
      case "Email":
        let counter = 1;
        if (applicant) {
          counter++;
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-email-field > a:text-is("${editCaseApplicantDetailsObject_content.emailAddress}")`,
            ),
            1,
          );
        }
        if (representative) {
          counter++;
          await commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-email-field > a:text-is("${editCaseRepresentativeDetailsObject_content.emailAddress}")`,
            ),
            1,
          );
        }
        await Promise.all([
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-fixed-radio-list-field > span.text-16:text-is("${editCaseSubjectDetailsObject_content.textOnPage9}")`,
            ),
            counter,
          ),
          commonHelpers.checkVisibleAndPresent(
            page.locator(
              `ccd-field-read-label > div > ccd-read-email-field > a:text-is("${editCaseSubjectDetailsObject_content.emailAddress}")`,
            ),
            1,
          ),
        ]);
        break;
      default:
        console.log("You have not selected a valid contact type.");
        process.exit(1);
    }
  },

  async handleApplicantInfo(page: Page): Promise<void> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `tbody > tr > td > span.text-16:text-is("${editCaseObjectsContacts_content.textOnPage4}")`,
        ),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${editCaseApplicantDetailsObject_content.name}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${editCaseApplicantDetailsObject_content.contactNumber}")`,
        ),
        1,
      ),
    ]);
  },

  async handleRepresentativeInfo(
    page: Page,
    representativeQualified: boolean,
  ): Promise<number[]> {
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `tbody > tr > td > span.text-16:text-is("${editCaseObjectsContacts_content.textOnPage6}")`,
        ),
        2,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${editCaseRepresentativeDetailsObject_content.name}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${editCaseRepresentativeDetailsObject_content.organisation}")`,
        ),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `ccd-field-read-label > div > ccd-read-text-field > span.text-16:text-is("${editCaseRepresentativeDetailsObject_content.contactNumber}")`,
        ),
        1,
      ),
    ]);
    let yes = 0;
    let no = 0;
    if (representativeQualified) {
      yes++;
    } else {
      no++;
    }
    return [yes, no];
  },
};

export default submitPage;
