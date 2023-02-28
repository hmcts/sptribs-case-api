package uk.gov.hmcts.sptribs.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class CreateHearingSummary extends Base {
    @Test
    public void caseWorkerShouldBeAbleToCreateHearingSummaryAndViewDetailsInHearingTab() {
        Login login = new Login();
        login.loginAsStTest1User();

        Case newCase = new Case();
        newCase.createCase("representative");
        newCase.buildCase();

        Hearing hearing = new Hearing();
        hearing.createListing();
        hearing.createHearingSummary();
        getTabByText("Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingType = PageHelpers.getValueFromTableFor("Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor("Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String judge = PageHelpers.getValueFromTableFor("Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = PageHelpers.getValueFromTableFor("Name of the panel member");
        Assertions.assertEquals("Ivy-Rose Rayner", panelMember);
        String otherAttendee = PageHelpers.getValueFromTableFor("Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }
}
