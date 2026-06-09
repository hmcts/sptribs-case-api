import { Page } from "@playwright/test";
import {
  caseRegionCode,
  hearingFormat,
  hearingSession,
  hearingType,
  hearingVenues,
} from "../../helpers/commonHelpers.ts";
import createListingHearingTypeAndFormatPage from "../../pages/CaseAPI/createListing/createListingHearingTypeAndFormatPage.ts";
import createListingRegionInfoPage from "../../pages/CaseAPI/createListing/createListingRegionInfoPage.ts";
import createListingListingDetailsPage from "../../pages/CaseAPI/createListing/createListingListingDetailsPage.ts";
import createListingRemoteHearingInformationPage from "../../pages/CaseAPI/createListing/createListingRemoteHearingInformationPage.ts";
import createListingOtherInformationPage from "../../pages/CaseAPI/createListing/createListingOtherInformationPage.ts";
import createListingNotifyPage from "../../pages/CaseAPI/createListing/createListingNotifyPage.ts";
import submitPage from "../../pages/CaseAPI/createListing/submitPage.ts";
import confirmPage from "../../pages/CaseAPI/createListing/confirmPage.ts";

type CreateListing = {
  createListing(
    page: Page,
    accessibilityTest: boolean,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    errorMessaging: boolean,
    caseNumber: string,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void>;
};

const createListing: CreateListing = {
  async createListing(
    page: Page,
    accessibilityTest: boolean,
    region: boolean,
    caseRegionCode: caseRegionCode | null,
    hearingType: hearingType,
    hearingFormat: hearingFormat,
    hearingSession: hearingSession,
    hearingAcrossMultipleDays: boolean,
    venue: hearingVenues | null,
    errorMessaging: boolean,
    caseNumber: string,
    subjectName: string,
    DSSSubmitted: boolean,
  ): Promise<void> {
    await createListingHearingTypeAndFormatPage.checkPageLoads(
      page,
      caseNumber,
      accessibilityTest,
      subjectName,
    );
    switch (errorMessaging) {
      default:
        await createListingHearingTypeAndFormatPage.fillInFields(
          page,
          hearingType,
          hearingFormat,
        );
        await createListingHearingTypeAndFormatPage.continueOn(page);
        await createListingRegionInfoPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
        );
        await createListingRegionInfoPage.fillInFields(
          page,
          region,
          caseRegionCode,
        );
        await createListingRegionInfoPage.continueOn(page);
        await createListingListingDetailsPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          errorMessaging,
          subjectName,
        );
        await createListingListingDetailsPage.fillInFields(
          page,
          venue,
          hearingSession,
          hearingAcrossMultipleDays,
        );
        await createListingListingDetailsPage.continueOn(page);
        await createListingRemoteHearingInformationPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
        );
        await createListingRemoteHearingInformationPage.fillInFields(page);
        await createListingRemoteHearingInformationPage.continueOn(page);
        await createListingOtherInformationPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
        );
        await createListingOtherInformationPage.fillInFields(page);
        await createListingOtherInformationPage.continueOn(page);
        await createListingNotifyPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
          DSSSubmitted,
        );
        break;
      case true:
        await createListingHearingTypeAndFormatPage.triggerErrorMessage(page);
        await createListingHearingTypeAndFormatPage.fillInFields(
          page,
          hearingType,
          hearingFormat,
        );
        await createListingHearingTypeAndFormatPage.continueOn(page);
        await createListingRegionInfoPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
        );
        await createListingRegionInfoPage.fillInFields(
          page,
          region,
          caseRegionCode,
        );
        await createListingRegionInfoPage.continueOn(page);
        await createListingListingDetailsPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          errorMessaging,
          subjectName,
        );
        await createListingListingDetailsPage.triggerErrorMessages(page);
        await createListingListingDetailsPage.fillInFields(
          page,
          venue,
          hearingSession,
          hearingAcrossMultipleDays,
        );
        await createListingListingDetailsPage.continueOn(page);
        await createListingRemoteHearingInformationPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
        );
        await createListingRemoteHearingInformationPage.fillInFields(page);
        await createListingRemoteHearingInformationPage.continueOn(page);
        await createListingOtherInformationPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
        );
        await createListingOtherInformationPage.fillInFields(page);
        await createListingOtherInformationPage.continueOn(page);
        await createListingNotifyPage.checkPageLoads(
          page,
          caseNumber,
          accessibilityTest,
          subjectName,
          DSSSubmitted,
        );
        await createListingNotifyPage.triggerErrorMessages(page);
        break;
    }
    await createListingNotifyPage.continueOn(page);
    await submitPage.checkPageLoads(
      page,
      caseNumber,
      region,
      hearingAcrossMultipleDays,
      venue,
      accessibilityTest,
      subjectName,
      DSSSubmitted,
    );
    await submitPage.checkValidInfo(
      page,
      region,
      caseRegionCode,
      hearingType,
      hearingFormat,
      hearingSession,
      hearingAcrossMultipleDays,
      venue,
      DSSSubmitted,
    );
    await submitPage.continueOn(page);
    await confirmPage.checkPageLoads(
      page,
      caseNumber,
      accessibilityTest,
      subjectName,
      DSSSubmitted,
    );
    await confirmPage.continueOn(page);
  },
};

export default createListing;
