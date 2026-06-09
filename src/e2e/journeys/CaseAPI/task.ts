import { Page } from "@playwright/test";
import { UserRole } from "../../config.ts";
import caseAPILoginPage from "../../pages/CaseAPI/caseList/caseAPILoginPage.ts";
import myWorkPage from "../../pages/WA/myWorkPage.ts";
import commonHelpers from "../../helpers/commonHelpers.ts";
import config from "../../config.ts";
import {
  allEvents,
  taskCompletionMethod,
} from "../../helpers/commonHelpers.ts";
import tasksPage from "../../pages/WA/tasksPage.ts";
import historyPage from "../../pages/WA/historyPage.ts";

type Task = {
  seeTask(
    page: Page,
    user: UserRole,
    accessibilityTest: boolean,
    taskName: string,
    subjectName: string,
  ): Promise<any>;
  initiateTask(
    page: Page,
    user: UserRole,
    taskCompletionMethod: taskCompletionMethod,
    accessibilityTest: boolean,
    caseNumber: string,
    taskName: string,
    priority: any,
    assignedUser: string,
    numberOfDays: number,
    event: allEvents,
    stateBeforeCompletion: string,
    subjectName: string,
  ): Promise<void>;
  checkCompletedTask(
    page: Page,
    accessibilityTest: boolean,
    taskName: string,
    caseNumber: string,
    stateAfterCompletion: string,
    checkCompletedTask: string,
  ): Promise<any>;
  removeTask(
    page: Page,
    caseNumber: string,
    taskRemoved: string,
    subjectName: string,
    user: any,
  ): Promise<void>;
};

const task: Task = {
  async seeTask(
    page: Page,
    user: UserRole,
    accessibilityTest: boolean,
    taskName: string,
    subjectName: string,
  ): Promise<any> {
    await page.locator(`a:text-is(" Sign out ")`).click();
    await page.waitForTimeout(5000);
    await page.waitForLoadState("domcontentloaded");
    await caseAPILoginPage.SignInUser(page, user);
    await myWorkPage.checkPageLoads(page, accessibilityTest, user);
    await myWorkPage.selectAvailableTasks(page, user);
    await myWorkPage.seeTask(page, taskName, subjectName);
  },

  async initiateTask(
    page: Page,
    user: UserRole,
    taskCompletionMethod: taskCompletionMethod,
    accessibilityTest: boolean,
    caseNumber: string,
    taskName: string,
    priority: any,
    assignedUser: string,
    numberOfDays: number,
    event: any,
    stateBeforeCompletion: string,
    subjectName: string,
  ): Promise<void> {
    switch (taskCompletionMethod) {
      default: //"Link: Assign Task to Me and Go To Task"
        await commonHelpers.signOutAndGoToCase(
          page,
          user,
          config.CaseAPIBaseURL,
          caseNumber,
        );
        await tasksPage.navigateToTaskTab(page, caseNumber);
        await tasksPage.assignTaskToMe(page, taskName);
        await historyPage.navigateToHistoryTab(page);
        await historyPage.checkStateBeforeTaskCompletion(
          page,
          accessibilityTest,
          stateBeforeCompletion,
        );
        await tasksPage.navigateToTaskTab(page, caseNumber);
        await tasksPage.clickTaskLink(page, event, taskName);
        break;
      case "Link: Assign Task to Me":
        await commonHelpers.signOutAndGoToCase(
          page,
          user,
          config.CaseAPIBaseURL,
          caseNumber,
        );
        await tasksPage.navigateToTaskTab(page, caseNumber);
        await tasksPage.assignTaskToMe(page, taskName);
        await historyPage.navigateToHistoryTab(page);
        await historyPage.checkStateBeforeTaskCompletion(
          page,
          accessibilityTest,
          stateBeforeCompletion,
        );
        await tasksPage.navigateToTaskTab(page, caseNumber);
        await tasksPage.clickTaskLink(page, event, taskName);
        break;
      case "Event DropDown":
        await commonHelpers.signOutAndGoToCase(
          page,
          user,
          config.CaseAPIBaseURL,
          caseNumber,
        );
        await tasksPage.navigateToTaskTab(page, caseNumber);
        await tasksPage.assignTaskToMe(page, taskName);
        await historyPage.navigateToHistoryTab(page);
        await historyPage.checkStateBeforeTaskCompletion(
          page,
          accessibilityTest,
          stateBeforeCompletion,
        );
        await tasksPage.navigateToTaskTab(page, caseNumber);
        await tasksPage.chooseEventFromDropdown(page, event);
        break;
    }
  },
  async checkCompletedTask(
    page: Page,
    accessibilityTest: boolean,
    taskName: string,
    caseNumber: string,
    stateAfterCompletion: string,
    subjectName: string,
  ): Promise<any> {
    await historyPage.navigateToHistoryTab(page);
    await historyPage.checkStateAfterTaskCompletion(page, stateAfterCompletion);
    await tasksPage.completedTaskNotVisible(
      page,
      caseNumber,
      taskName,
      subjectName,
    );
  },

  async removeTask(
    page,
    caseNumber,
    taskRemoved,
    subjectName,
    user,
  ): Promise<void> {
    await commonHelpers.signOutAndGoToCase(
      page,
      user,
      config.CaseAPIBaseURL,
      caseNumber,
    );
    await tasksPage.navigateToTaskTab(page, caseNumber);
    await tasksPage.cancelTask(page, taskRemoved);
  },
};

export default task;
