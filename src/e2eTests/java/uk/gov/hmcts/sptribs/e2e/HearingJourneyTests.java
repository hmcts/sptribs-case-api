package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.loadStateOptionsWithTimeout;

public class HearingJourneyTests extends Base {
    private String caseNumber;

    @Disabled
    public void caseWorkerShouldBeAbleToEditListingAndViewDetailsInHearingTab() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        caseNumber = newCase.createCase("representative");
        newCase.buildCase();

        Hearing hearing = new Hearing(page);
        hearing.createListing();
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

    @Disabled
    @Order(1)
    public void caseWorkerShouldBeAbleToCreateHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        caseNumber = newCase.createCase("representative");
        newCase.buildCase();

        Hearing hearing = new Hearing(page);
        hearing.createListing();
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

    @Disabled
    @Order(2)
    public void caseWorkerShouldBeAbleToEditHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        page.navigate(getCaseUrl(caseNumber));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED,loadStateOptionsWithTimeout(90000));
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(90000));

        Hearing hearing = new Hearing(page);
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
        Assertions.assertEquals("Ivy-Rose Rayner", panelMember);
        String otherAttendee = PageHelpers.getValueFromTableFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @Disabled
    public void caseWorkerShouldBeAbleToPostponeHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();

        Hearing hearing = new Hearing(page);
        hearing.createListing();
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

    @Disabled
    public void caseWorkerShouldBeAbleToCancelHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();

        Hearing hearing = new Hearing(page);
        hearing.createListing();
        hearing.cancelHearing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Hearing Status");
        Assertions.assertEquals("Cancelled", hearingStatus);
    }
}
