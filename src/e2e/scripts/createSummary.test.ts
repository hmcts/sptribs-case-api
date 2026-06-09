import { test } from "@playwright/test";
import events_content from "../fixtures/content/CaseAPI/events_content.ts";
import taskNames_content from "../fixtures/content/taskNames_content.ts";
import waUsers_content from "../fixtures/content/waUsers_content.ts";
import commonHelpers from "../helpers/commonHelpers.ts";
import buildCase from "../journeys/CaseAPI/buildCase.ts";
import createCase from "../journeys/CaseAPI/createCase.ts";
import createListing from "../journeys/CaseAPI/createListing.ts";
import createSummary from "../journeys/CaseAPI/createSummary.ts";
import task from "../journeys/CaseAPI/task.ts";

test.describe("Create hearing summary tests", (): void => {
  test.only("Create hearing summary - hearing outcome is allowed.", async ({
    page,
  }): Promise<void> => {
    const subjectName = `Subject AutoTesting${commonHelpers.randomLetters(5)}`;
    const caseNumber1102 = await createCase.createCase(
      page,
      waUsers_content.userRoleAdmin,
      false,
      "Assessment",
      "Other",
      true,
      true,
      "Email",
      subjectName,
      true,
      false,
      "1996",
      "Scotland",
      true,
      true,
      true,
      false,
      true,
      false,
    );
    await commonHelpers.chooseEventFromDropdown(page, events_content.buildCase);
    await buildCase.buildCase(page, false, caseNumber1102, subjectName);
    await task.removeTask(
      page,
      caseNumber1102,
      taskNames_content.issueCaseToRespondentTask,
      subjectName,
      waUsers_content.userRoleAdmin,
    );
    await commonHelpers.chooseEventFromDropdown(
      page,
      "Hearings: Create listing",
    );
    await createListing.createListing(
      page,
      false,
      true,
      "1-London",
      "Interlocutory",
      "Face to Face",
      "Morning",
      false,
      null,
      false,
      caseNumber1102,
      subjectName,
      false,
    );
    await createSummary.createSummary(
      page,
      false,
      "Interlocutory",
      "Face to Face",
      "Morning",
      false,
      null,
      null,
      "Allowed",
      null,
      false,
      true,
      false,
      caseNumber1102,
      subjectName,
    );
  });
})