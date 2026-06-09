import { Page } from "@playwright/test";
import commonHelpers, {
  hearingAdjournedReasons,
  hearingOutcome,
  hearingVenues,
  hearingType,
  hearingFormat,
  hearingSession,
  hearingVenueNames,
} from "../../helpers/commonHelpers.ts";
import createSummarySelectHearingPage from "../../pages/CaseAPI/createSummary/createSummarySelectHearingPage.ts";
import createSummaryHearingTypeAndFormatPage from "../../pages/CaseAPI/createSummary/createSummaryHearingTypeAndFormatPage.ts";
import createSummaryListingDetailsPage from "../../pages/CaseAPI/createSummary/createSummaryListingDetailsPage.ts";
import createSummaryHearingAttendeesPage from "../../pages/CaseAPI/createSummary/createSummaryHearingAttendeesPage.ts";
import createSummaryHearingAttendeesRolePage from "../../pages/CaseAPI/createSummary/createSummaryHearingAttendeesRolePage.ts";
import createSummaryHearingOutcomePage from "../../pages/CaseAPI/createSummary/createSummaryHearingOutcomePage.ts";
import createSummaryHearingRecordingUploadPage from "../../pages/CaseAPI/createSummary/createSummaryHearingRecordingUploadPage.ts";
import submitPage from "../../pages/CaseAPI/createSummary/submitPage.ts";
import confirmPage from "../../pages/CaseAPI/createSummary/confirmPage.ts";
import hearingsTabPage from "../../pages/CaseAPI/caseTabs/hearingsTabPage.ts";
import hearingTabPage from "../../pages/CaseAPI/caseTabs/hearingsTabPage.ts";

type CreateSummary = {
  createSummary(
    page: Page,
    accessibilityTest: boolean,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: string,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    fullPanelHearing: boolean,
    editJourney: boolean,
    errorMessaging: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<string | void>;
};

const createSummary: CreateSummary = {
  async createSummary(
    page: Page,
    accessibilityTest: boolean,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    venueName: hearingVenueNames | null,
    hearingOutcome: hearingOutcome,
    hearingAdjournedReason: hearingAdjournedReasons | null,
    fullPanelHearing: boolean,
    editJourney: boolean,
    errorMessaging: boolean,
    caseNumber: string,
    subjectName: string,
  ): Promise<string | void> {
    await commonHelpers.chooseEventFromDropdown(
      page,
      "Hearings: Create summary",
    );
    if (caseNumber !== undefined) {
      switch (errorMessaging) {
        default:
          await createSummarySelectHearingPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          const hearing =
            await createSummarySelectHearingPage.fillInFields(page);
          await createSummarySelectHearingPage.continueOn(page);
          await createSummaryHearingTypeAndFormatPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await createSummaryHearingTypeAndFormatPage.checkFields(
            page,
            hearingType,
            hearingFormat,
            editJourney,
          );
          await createSummaryHearingTypeAndFormatPage.continueOn(page);
          await createSummaryListingDetailsPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            hearingAcrossMultipleDays,
            venue,
            subjectName,
          );
          await createSummaryListingDetailsPage.checkFields(
            page,
            venue,
            venueName,
            hearingSession,
            hearingAcrossMultipleDays,
            editJourney,
          );
          await createSummaryListingDetailsPage.continueOn(page);
          await createSummaryHearingAttendeesPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            errorMessaging,
            subjectName,
          );
          const panel = await createSummaryHearingAttendeesPage.fillFields(
            page,
            fullPanelHearing,
          );
          await createSummaryHearingAttendeesPage.continueOn(page);
          await createSummaryHearingAttendeesRolePage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await createSummaryHearingAttendeesRolePage.fillFields(page);
          await createSummaryHearingAttendeesRolePage.continueOn(page);
          await createSummaryHearingOutcomePage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            errorMessaging,
            subjectName,
          );
          await createSummaryHearingOutcomePage.fillFields(
            page,
            hearingOutcome,
            hearingAdjournedReason,
          );
          await createSummaryHearingOutcomePage.continueOn(page);
          await createSummaryHearingRecordingUploadPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await createSummaryHearingRecordingUploadPage.fillFields(page);
          await createSummaryHearingRecordingUploadPage.continueOn(page);
          await submitPage.checkPageLoads(
            page,
            caseNumber,
            hearingAcrossMultipleDays,
            fullPanelHearing,
            hearingOutcome,
            hearingAdjournedReason,
            venue,
            editJourney,
            accessibilityTest,
            subjectName,
          );
          await submitPage.checkValidInfo(
            page,
            panel,
            fullPanelHearing,
            hearing,
            hearingType,
            hearingFormat,
            hearingSession,
            hearingAcrossMultipleDays,
            hearingOutcome,
            hearingAdjournedReason,
            venue,
            venueName,
            editJourney,
          );
          await submitPage.continueOn(page);
          await confirmPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await confirmPage.continueOn(page);
          // await hearingsTabPage.changeToHearingsTab(page);
          // await hearingsTabPage.checkPageLoads(
          //   page,
          //   true,
          //   hearingAcrossMultipleDays,
          //   false,
          //   venue,
          //   true,
          //   false,
          //   null,
          //   false,
          //   editJourney,
          //   false,
          //   false,
          //   false,
          //   accessibilityTest,
          // );
          // await hearingTabPage.checkValidInfoCreateSummary(
          //   page,
          //   hearingType,
          //   hearingFormat,
          //   hearingSession,
          //   hearingAcrossMultipleDays,
          //   venue,
          //   venueName,
          //   hearingOutcome,
          //   hearingAdjournedReason,
          //   panel,
          //   fullPanelHearing,
          //   editJourney,
          //   subjectName,
          // );
          break;
        case true:
          await createSummarySelectHearingPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await createSummarySelectHearingPage.triggerErrorMessages(page);
          await createSummarySelectHearingPage.fillInFields(page);
          await createSummarySelectHearingPage.continueOn(page);
          await createSummaryHearingTypeAndFormatPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await createSummaryHearingTypeAndFormatPage.checkFields(
            page,
            hearingType,
            hearingFormat,
            editJourney,
          );
          await createSummaryHearingTypeAndFormatPage.continueOn(page);
          await createSummaryListingDetailsPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            hearingAcrossMultipleDays,
            venue,
            subjectName,
          );
          await createSummaryListingDetailsPage.triggerErrorMessages(page);
          await createSummaryListingDetailsPage.continueOn(page);
          await createSummaryHearingAttendeesPage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            errorMessaging,
            subjectName,
          );
          await createSummaryHearingAttendeesPage.triggerErrorMessages(page);
          await createSummaryHearingAttendeesPage.continueOn(page);
          await createSummaryHearingAttendeesRolePage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            subjectName,
          );
          await createSummaryHearingAttendeesRolePage.triggerErrorMessages(
            page,
          );
          await createSummaryHearingAttendeesRolePage.fillFields(page);
          await createSummaryHearingAttendeesRolePage.continueOn(page);
          await createSummaryHearingOutcomePage.checkPageLoads(
            page,
            caseNumber,
            accessibilityTest,
            errorMessaging,
            subjectName,
          );
          await createSummaryHearingOutcomePage.triggerErrorMessages(page);
      }
    } else {
      throw new Error("Case number is undefined.");
    }
  },
};

export default createSummary;
