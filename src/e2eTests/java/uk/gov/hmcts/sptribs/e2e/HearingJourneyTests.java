package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class HearingJourneyTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToEditListingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.editListing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Listed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Interlocutory", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Video", hearingFormat);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToCreateHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        final HashMap<String, String> map = hearing.createHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String displayedJudge = getValueFromTableWithinHearingsTabFor(page, "Which judge heard the case?");
        Assertions.assertEquals(map.get("judge"), displayedJudge);
        String displayedPanelMember = getValueFromTableWithinHearingsTabFor(page, "Name of the panel member");
        Assertions.assertEquals(map.get("panelMember"), displayedPanelMember);
        String otherAttendee = getValueFromTableWithinHearingsTabFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToEditHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.createHearingSummary();
        final HashMap<String, String> map =  hearing.editHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Final", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Hybrid", hearingFormat);
        String displayedJudge = getValueFromTableWithinHearingsTabFor(page, "Which judge heard the case?");
        Assertions.assertEquals(map.get("judge"), displayedJudge);
        String displayedPanelMember = getValueFromTableWithinHearingsTabFor(page, "Name of the panel member");
        Assertions.assertEquals(map.get("panelMember"), displayedPanelMember);
        String otherAttendee = getValueFromTableWithinHearingsTabFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToPostponeHearingAndViewDetailsInHearingTab() {

        Page page = getPage();
        createAndBuildCase(page);
        Hearing hearing = createListing(page);
        hearing.postponeHearing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Postponed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String postponeReason = getValueFromTableWithinHearingsTabFor(page, "Reason Postponed");
        Assertions.assertEquals("Extension granted", postponeReason);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToCancelHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.cancelHearing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Cancelled", hearingStatus);
    }

    private void createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsCaseWorker();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
    }


    private Hearing createListing(Page page) {
        Hearing hearing = new Hearing(page);
        hearing.createListing();
        return hearing;
    }

    private static String getValueFromTableWithinHearingsTabFor(Page page, String rowHeader) {
        return page.locator("#case-viewer-field-read--hearingList th:has-text(\"" + rowHeader + "\") + td").textContent();
    }
}
