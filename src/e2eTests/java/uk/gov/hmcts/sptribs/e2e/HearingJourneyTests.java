package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class HearingJourneyTests extends Base {

    @Test
    public void caseWorkerShouldBeAbleToEditListingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.editListing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Hearing Status");
        Assertions.assertEquals("Listed", hearingStatus);
        String hearingType = PageHelpers.getValueFromTableFor(page, "Hearing type");
        Assertions.assertEquals("Interlocutory", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor(page, "Hearing format");
        Assertions.assertEquals("Video", hearingFormat);
    }

    @Test
    public void caseWorkerShouldBeAbleToCreateHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.createHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = PageHelpers.getValueFromTableFor(page, "Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor(page, "Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String judge = PageHelpers.getValueFromTableFor(page, "Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = PageHelpers.getValueFromTableFor(page, "Name of the panel member");
        Assertions.assertEquals("Ivy-Rose Rayner", panelMember);
        String otherAttendee = PageHelpers.getValueFromTableFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @Test
    public void caseWorkerShouldBeAbleToEditHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.createHearingSummary();
        hearing.editHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = PageHelpers.getValueFromTableFor(page, "Hearing type");
        Assertions.assertEquals("Final", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor(page, "Hearing format");
        Assertions.assertEquals("Hybrid", hearingFormat);
        String judge = PageHelpers.getValueFromTableFor(page, "Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = PageHelpers.getValueFromTableFor(page, "Name of the panel member");
        Assertions.assertEquals("Joe Bloggs", panelMember);
        String otherAttendee = PageHelpers.getValueFromTableFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @Test
    public void caseWorkerShouldBeAbleToPostponeHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.postponeHearing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Hearing Status");
        Assertions.assertEquals("Postponed", hearingStatus);
        String hearingType = PageHelpers.getValueFromTableFor(page, "Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor(page, "Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        assertThat(page.locator("h4").last()).hasText("Postponement summary");
        String postponeReason = PageHelpers.getValueFromTableFor(page, "Postpone Reason");
        Assertions.assertEquals("Appellant not ready to proceed", postponeReason);
    }

    @Test
    public void caseWorkerShouldBeAbleToCancelHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page);

        Hearing hearing = createListing(page);
        hearing.cancelHearing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Hearing Status");
        Assertions.assertEquals("Cancelled", hearingStatus);
    }

    private void createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
    }

    private Hearing createListing(Page page) {
        Hearing hearing = new Hearing(page);
        hearing.createListing();
        return hearing;
    }
}
