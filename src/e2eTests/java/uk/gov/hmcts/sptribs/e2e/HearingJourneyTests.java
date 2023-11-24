package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.Representative;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.DssSubmitted;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.visibleOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class HearingJourneyTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToEditListingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page, Representative);

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
        createAndBuildCase(page, Representative);

        Hearing hearing = createListing(page);
        hearing.createHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String judge = getValueFromTableWithinHearingsTabFor(page, "Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = getValueFromTableWithinHearingsTabFor(page, "Name of the panel member");
        Assertions.assertEquals("Joe Bloggs", panelMember);
        String otherAttendee = getValueFromTableWithinHearingsTabFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @Disabled
    public void caseWorkerShouldBeAbleToCreateHearingSummaryAndViewDetailsInHearingTabForDSSCase() {
        Page page = getPage();
        createEditAndBuildDssCase(page);

        Hearing hearing = createListing(page);
        hearing.createHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String judge = getValueFromTableWithinHearingsTabFor(page, "Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = getValueFromTableWithinHearingsTabFor(page, "Name of the panel member");
        Assertions.assertEquals("Joe Bloggs", panelMember);
        String otherAttendee = getValueFromTableWithinHearingsTabFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToEditHearingSummaryAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page, Representative);

        Hearing hearing = createListing(page);
        hearing.createHearingSummary();
        hearing.editHearingSummary();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Completed", hearingStatus);
        String hearingType = getValueFromTableWithinHearingsTabFor(page, "Hearing type");
        Assertions.assertEquals("Final", hearingType);
        String hearingFormat = getValueFromTableWithinHearingsTabFor(page, "Hearing format");
        Assertions.assertEquals("Hybrid", hearingFormat);
        String judge = getValueFromTableWithinHearingsTabFor(page, "Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = getValueFromTableWithinHearingsTabFor(page, "Name of the panel member");
        Assertions.assertEquals("Joe Bloggs", panelMember);
        String otherAttendee = getValueFromTableWithinHearingsTabFor(page, "Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToPostponeHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page, Representative);

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
        String postponeReason = getValueFromTableWithinHearingsTabFor(page, "Postpone Reason");
        Assertions.assertEquals("Extension granted", postponeReason);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToCancelHearingAndViewDetailsInHearingTab() {
        Page page = getPage();
        createAndBuildCase(page, Representative);

        Hearing hearing = createListing(page);
        hearing.cancelHearing();
        getTabByText(page, "Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingStatus = getValueFromTableWithinHearingsTabFor(page, "Hearing Status");
        Assertions.assertEquals("Cancelled", hearingStatus);
    }


    private void createEditAndBuildDssCase(Page page) {
        DssCase newDssCase = new DssCase(page);
        newDssCase.navigateToDssLoginPage();
        Login login = new Login(page);
        login.loginAsStCitizen1User();
        final String caseNumber = newDssCase.createCase("representative");
        clickLink(page, "Sign out");
        page.navigate(CASE_API_BASE_URL, new Page.NavigateOptions().setTimeout(90000));
        login.loginAsCaseWorker();
        page.navigate(getCaseUrl(caseNumber));
        Case dssCase = new Case(page);
        assertThat(page.locator(".mat-tab-list")).isVisible(visibleOptionsWithTimeout(60000));
        Assertions.assertEquals(DssSubmitted.label, dssCase.getCaseStatus());
        dssCase.editDssCase("representative","applicant");
        dssCase.buildCase();
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
