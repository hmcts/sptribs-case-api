package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.Base.page;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getRadioButtonByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class Hearing {
    /*
    createListing() method accepts String arguments. By default, this method creates
    summary with hearing venue from drop-down list. To create summary with
    hearing venue manually pass the parameter "entervenue" to the method.
     */
    public void createListing(String... args) {
        Case newCase = new Case();
        newCase.startNextStepAction("Hearings: Create listing");

        // Fill hearing type and format form
        assertThat(page.locator("h1"))
            .hasText("Hearing type and format", textOptionsWithTimeout(30000));
        page.getByLabel("Case management").check();
        page.getByLabel("Face to Face").check();
        PageHelpers.clickButton("Continue");

        // Fill Region data form
        assertThat(page.locator("h1"))
            .hasText("Region Data", textOptionsWithTimeout(30000));
        page.selectOption("#recordRegionList", new SelectOption().setLabel("1-London"));
        PageHelpers.clickButton("Continue");

        // Fill Hearing location and duration form
        assertThat(page.locator("h1"))
            .hasText("Hearing location and duration", textOptionsWithTimeout(30000));

        List<String> options = Arrays.stream(args)
            .map(arg -> arg.replace(" ", "").toLowerCase())
            .collect(Collectors.toList());
        if (options.contains("entervenue")) {
            getCheckBoxByLabel("Venue not listed").check();
            getTextBoxByLabel("Hearing Venue").last()
                .fill("Hendon Magistrates Court, The Court House, The Hyde");
        } else {
            page.selectOption("#recordHearingVenues",
                new SelectOption().setLabel("CROYDON MAGISTRATES COURT-BARCLAY ROAD"));
        }
        getTextBoxByLabel("Room at venue (Optional)").fill("The Court Room");
        getTextBoxByLabel("Additional instructions and directions (Optional)")
            .fill("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        Calendar date = DateHelpers.getFutureDate(30);
        getTextBoxByLabel("Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel("Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel("Year").fill(String.valueOf(date.get(Calendar.YEAR)));
        getTextBoxByLabel("Start time (24hr format)").fill("10:10");
        getRadioButtonByLabel("Morning").click();
        getRadioButtonByLabel("No").last().click();
        clickButton("Continue");

        // Fill Remote hearing information form
        assertThat(page.locator("h1"))
            .hasText("Remote hearing information", textOptionsWithTimeout(20000));
        getTextBoxByLabel("Video call link (Optional)").fill("http://localhost:3000");
        getTextBoxByLabel("Conference call number (Optional)").fill("01762534632");
        clickButton("Continue");

        // Fill Other information form
        assertThat(page.locator("h1"))
            .hasText("Other information", textOptionsWithTimeout(20000));
        getTextBoxByLabel("Other important information (Optional)")
            .fill("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        clickButton("Continue");

        // Fill Notify parties form
        assertThat(page.locator("h1"))
            .hasText("Notify parties", textOptionsWithTimeout(20000));
        getCheckBoxByLabel("Subject").check();
        if (page.isVisible("#cicCaseNotifyPartyRepresentative-RepresentativeCIC")) {
            getCheckBoxByLabel("Representative").check();
        }
        getCheckBoxByLabel("Respondent").check();
        clickButton("Continue");

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton("Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Listing record created", textOptionsWithTimeout(60000));
        clickButton("Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Awaiting hearing", newCase.getCaseStatus());
    }

    public void createHearingSummary() {
        Case newCase = new Case();
        newCase.startNextStepAction("Hearings: Create summary");

        // Fill Select hearing form
        assertThat(page.locator("h1"))
            .hasText("Select hearing", textOptionsWithTimeout(30000));
        page.selectOption("#cicCaseHearingList", new SelectOption().setIndex(1));
        PageHelpers.clickButton("Continue");

        // Fill hearing type and format form
        assertThat(page.locator("h1"))
            .hasText("Hearing type and format", textOptionsWithTimeout(30000));
        assertThat(getRadioButtonByLabel("Case management")).isChecked();
        assertThat(getRadioButtonByLabel("Face to Face")).isChecked();
        PageHelpers.clickButton("Continue");

        // Fill Hearing location and duration form
        assertThat(page.locator("h1"))
            .hasText("Hearing location and duration", textOptionsWithTimeout(30000));
        PageHelpers.clickButton("Continue");

        // Fill Hearing attendees form
        assertThat(page.locator("h1"))
            .hasText("Hearing attendees", textOptionsWithTimeout(30000));
        page.selectOption("#hearingSummaryJudge", new SelectOption().setLabel("Chetan Lad"));
        getRadioButtonByLabel("Yes").click();
        page.selectOption("#hearingSummaryPanelMemberList_0_name", new SelectOption().setLabel("Ivy-Rose Rayner"));
        getRadioButtonByLabel("Full member").click();
        PageHelpers.clickButton("Continue");

        // Fill Hearing attendees second form
        assertThat(page.locator("h1"))
            .hasText("Hearing attendees", textOptionsWithTimeout(30000));
        getCheckBoxByLabel("Appellant").first().check();
        getCheckBoxByLabel("Observer").check();
        getCheckBoxByLabel("Other").check();
        getTextBoxByLabel("Who was this other attendee?").fill("Special officer");
        PageHelpers.clickButton("Continue");

        // Fill Hearing outcome form
        assertThat(page.locator("h1"))
            .hasText("Hearing outcome", textOptionsWithTimeout(30000));
        getRadioButtonByLabel("Adjourned").click();
        PageHelpers.clickButton("Continue");

        // Fill Upload hearing recording form
        assertThat(page.locator("h1"))
            .hasText("Upload hearing recording", textOptionsWithTimeout(30000));
        getTextBoxByLabel("Where can the recording be found? (Optional)").fill("http://localhost:3000");
        PageHelpers.clickButton("Continue");

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton("Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Hearing summary created", textOptionsWithTimeout(60000));
        clickButton("Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Awaiting outcome", newCase.getCaseStatus());
    }
}
