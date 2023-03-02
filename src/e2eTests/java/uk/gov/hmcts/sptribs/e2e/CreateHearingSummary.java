package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class CreateHearingSummary extends Base {
    private Page page;

    @Test
    public void caseWorkerShouldBeAbleToCreateHearingSummaryAndViewDetailsInHearingTab() {
        page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();

        Hearing hearing = new Hearing(page);
        hearing.createListing();
        hearing.createHearingSummary();
        getTabByText(page,"Hearings").click();
        assertThat(page.locator("h4").first()).hasText("Listing details");
        String hearingType = PageHelpers.getValueFromTableFor(page,"Hearing type");
        Assertions.assertEquals("Case management", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor(page,"Hearing format");
        Assertions.assertEquals("Face to Face", hearingFormat);
        String judge = PageHelpers.getValueFromTableFor(page,"Which judge heard the case?");
        Assertions.assertEquals("Chetan Lad", judge);
        String panelMember = PageHelpers.getValueFromTableFor(page,"Name of the panel member");
        Assertions.assertEquals("Ivy-Rose Rayner", panelMember);
        String otherAttendee = PageHelpers.getValueFromTableFor(page,"Who was this other attendee?");
        Assertions.assertEquals("Special officer", otherAttendee);
    }
}
