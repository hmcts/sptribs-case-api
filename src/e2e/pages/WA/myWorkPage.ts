import { AxeUtils } from "@hmcts/playwright-common";
import { expect, Page } from "@playwright/test";
import config from "../../config.ts";
import myWork_content from "../../fixtures/content/CaseAPI/myWork/myWork_content.ts";
import waUsers_content from "../../fixtures/content/waUsers_content.ts";
import commonHelpers from "../../helpers/commonHelpers.ts";

type MyWorkPage = {
  myTasksTab: string;
  availableTasksTab: string;
  filterButton: string;
  assignToMeAndGoToTask: string;
  assignToMeLink: string;
  myWorkLink: string;
  availableTasksUrl: string;
  myTasksUrl: string;
  checkPageLoads(
    page: Page,
    accessibilityTest: boolean,
    user: any,
  ): Promise<void>;
  selectAvailableTasks(page: Page, user: any): Promise<void>;
  seeTask(page: Page, taskName: string, subjectName: string): Promise<void>;
  clickAssignAndGoToTask(
    page: Page,
    subjectName: string,
    taskName: string,
  ): Promise<void>;
  clickAssignToMe(
    page: Page,
    subjectName: string,
    taskName: string,
  ): Promise<void>;
  navigateToTaskPage(
    page: Page,
    taskName: string,
    subjectName: string,
  ): Promise<void>;
  navigateToMyWorkPage(page: Page): Promise<void>;
  dataCleanUpAssignTask(page: Page, selector: string): Promise<void>;
};

const myWorkPage: MyWorkPage = {
  myTasksTab: `a:text-is(" My tasks ")`,
  availableTasksTab: "li.hmcts-sub-navigation__item:nth-child(2)",
  filterButton: ".hmcts-button--secondary",
  assignToMeAndGoToTask: "#action_claim-and-go",
  assignToMeLink: "#action_claim",
  myWorkLink: `a.hmcts-primary-navigation__link:text-is(" My work ")`,
  availableTasksUrl: `${config.CaseAPIBaseURL.replace(/\/cases$/, "")}/work/my-work/available`,
  myTasksUrl: `${config.CaseAPIBaseURL.replace(/\/cases$/, "")}/work/my-work/list`,

  async checkPageLoads(page, accessibilityTest, user): Promise<void> {
    await page.waitForSelector(
      `.hmcts-primary-navigation__link:text-is("My work")`,
    );
    await page.locator(".hmcts-primary-navigation__link").first().click();
    await page.waitForSelector(
      `.govuk-heading-xl:text-is("${myWork_content.pageTitle}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(`p:text-is("${myWork_content.hintText}")`),
        1,
      ),
      ...Array.from({ length: 4 }, (_, index) => {
        const tab = (myWork_content as any)[`tab${index + 1}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`li.hmcts-sub-navigation__item:has-text("${tab}")`),
          1,
        );
      }),
    ]);

    if (user === waUsers_content.userRoleJudge) {
      await Promise.all([
        ...Array.from({ length: 6 }, (_, index) => {
          const judicialColumn = (myWork_content as any)[
            `judicialColumn${index + 1}`
          ];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(
              `th.cdk-header-cell > button:text-is("${judicialColumn}")`,
            ),
            1,
          );
        }),
      ]);
    } else {
      await Promise.all([
        ...Array.from({ length: 6 }, (_, index) => {
          const column = (myWork_content as any)[`column${index + 1}`];
          return commonHelpers.checkVisibleAndPresent(
            page.locator(`th.cdk-header-cell > button:text-is("${column}")`),
            1,
          );
        }),
      ]);
      expect(
        page.locator(`button > h1:text-is("${myWork_content.priorityColumn}")`),
      );
    }
    if (accessibilityTest) {
      await new AxeUtils(page).audit();
    }
  },

  async selectAvailableTasks(page: Page, user: any): Promise<void> {
    await page.locator(this.availableTasksTab).click();
    await page.waitForURL(/.*\/available$/, { timeout: 30_000 });
    await page.waitForTimeout(7000);

    if (user === waUsers_content.userRoleJudge) {
      await page
        .getByRole("button")
        .filter({ hasText: " Show work filter " })
        .dispatchEvent("click");

      await page.waitForSelector("xuilib-generic-filter > form");
      await page.locator("input#checkbox_servicesservices_all").click();
      await page.locator("input#checkbox_servicesST_CIC").click();
      await page.locator("button#applyFilter").click();
      await page.waitForTimeout(5000);
    }
  },

  async seeTask(
    page: Page,
    taskName: string,
    subjectName: string,
  ): Promise<any> {
    const subjectTask = page
      .locator("tr")
      .filter({
        has: page.locator(`td:has-text("${subjectName}")`),
      })
      .locator(`exui-task-field:text-is("${taskName}")`);
    const paginationLocator = page.locator('[aria-label="Pagination"]');

    while (true) {
      let locatorFound = false;
      if (await subjectTask.isVisible()) {
        break;
      }

      const nextPageButton = page.getByLabel("Next page", { exact: true });
      const nextPageButtonExists = await nextPageButton.count();

      if (nextPageButtonExists > 0) {
        await nextPageButton.click();
        await page.waitForTimeout(3000);
      } else {
        const pageOneButton = page.getByLabel("Page 1", { exact: true });
        const pageOneExists = await pageOneButton.count();

        if (pageOneExists > 0) {
          await pageOneButton.click();
          await page.waitForTimeout(3000);
        } else {
          await page.reload();
          await page.waitForTimeout(3000);
        }
      }
    }
  },

  async clickAssignAndGoToTask(
    page: Page,
    subjectName: string,
    taskName,
  ): Promise<void> {
    await page
      .locator("tr", {
        has: page.locator(`td:has-text("${subjectName}")`),
        hasText: taskName,
      })
      .locator(`td > div > button:has-text("Manage")`)
      .click();
    await page.waitForSelector(this.assignToMeAndGoToTask);
    await page.locator(this.assignToMeAndGoToTask).click();
    await page.waitForSelector(`h2:text-is("Active tasks")`);
  },

  async clickAssignToMe(
    page: Page,
    subjectName: string,
    taskName: string,
  ): Promise<void> {
    await page
      .locator("tr", {
        has: page.locator(`td:has-text("${subjectName}")`),
        hasText: taskName,
      })
      .locator(`td > div > button:has-text("Manage")`)
      .click();
    await page.waitForSelector(this.assignToMeLink);
    await page.locator(this.assignToMeLink).click();
    await page
      .locator(`td:has-text("${subjectName}")`)
      .waitFor({ state: "detached" });
  },

  async navigateToTaskPage(
    page: Page,
    taskName: string,
    subjectName: string,
  ): Promise<void> {
    const subjectAutoTesting = `exui-task-field:has-text("Subject AutoTesting")`;
    const subjectTask = page
      .locator("tr")
      .filter({
        has: page.locator(`td:has-text("${subjectName}")`),
      })
      .locator(
        `exui-task-field > exui-task-name-field > exui-url-field > a:text-is("${taskName}")`,
      );
    const paginationLocator = page.locator("ccd-pagination a span", {
      hasText: /^[1-9][0-9]*$/,
    });

    await page.locator(this.myTasksTab).click();
    await page.locator(subjectAutoTesting).first().waitFor();
    while (true) {
      let locatorFound = false;
      if (await subjectTask.isVisible()) {
        await subjectTask.click();
        break;
      }

      const paginationExists = (await paginationLocator.count()) > 0;
      if (!paginationExists) {
        await page.goto(this.myTasksUrl);
        await page.waitForTimeout(3000);
      } else {
        await page.getByLabel("Next Page").click();
        await page.waitForTimeout(3000);

        if (await subjectTask.isVisible()) {
          locatorFound = true;
          await subjectTask.click();
          break;
        }
        if (locatorFound) {
          await subjectTask.click();
          break;
        }
      }
    }
  },

  async navigateToMyWorkPage(page: Page): Promise<void> {
    await page.locator(this.myWorkLink).click();
    while (page.url().includes("service-down")) {
      await page.locator(this.myWorkLink).click();
    }
    await page.waitForSelector(`h3:text-is("My work")`);
  },

  async dataCleanUpAssignTask(page: Page, selector: any): Promise<void> {
    await selector.first().locator(`div > button:has-text("Manage")`).click();
    await page.waitForSelector(this.assignToMeLink);
    await page.locator(this.assignToMeLink).click();
    await page.waitForTimeout(3000);
  },
};

export default myWorkPage;
