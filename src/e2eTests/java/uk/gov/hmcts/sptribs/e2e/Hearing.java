package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.CaseState.AwaitingHearing;
import static uk.gov.hmcts.sptribs.e2e.CaseState.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.e2e.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getRadioButtonByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getValueFromTableFor;

public class Hearing {
    private final Page page;

    public Hearing(Page page) {
        this.page = page;
    }

    /*
    createListing() method accepts String arguments. By default, this method creates
    summary with hearing venue from drop-down list. To create summary with
    hearing venue manually pass the parameter "entervenue" to the method.
     */
    public void createListing(String... args) {
        Case newCase = new Case(page);
        newCase.startNextStepAction("Hearings: Create listing");

        // Fill hearing type and format form
        assertThat(page.locator("h1"))
            .hasText("Hearing type and format", textOptionsWithTimeout(60000));
        getRadioButtonByLabel(page, "Case management").check();
        getRadioButtonByLabel(page, "Face to Face").check();
        PageHelpers.clickButton(page, "Continue");

        // Fill Region data form
        assertThat(page.locator("h1"))
            .hasText("Region Data", textOptionsWithTimeout(30000));
        page.selectOption("#recordRegionList", new SelectOption().setLabel("1-London"));
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing location and duration form
        assertThat(page.locator("h1"))
            .hasText("Hearing location and duration", textOptionsWithTimeout(30000));

        List<String> options = Arrays.stream(args)
            .map(arg -> arg.replace(" ", "").toLowerCase()).toList();
        if (options.contains("entervenue")) {
            getCheckBoxByLabel(page, "Venue not listed").check();
            getTextBoxByLabel(page, "Hearing Venue").last()
                .fill("Hendon Magistrates Court, The Court House, The Hyde");
        } else {
            page.selectOption("#recordHearingVenues",
                new SelectOption().setLabel("CROYDON MAGISTRATES COURT-BARCLAY ROAD"));
        }
        getTextBoxByLabel(page, "Room at venue (Optional)").fill("The Court Room");
        getTextBoxByLabel(page, "Additional instructions and directions (Optional)")
            .fill("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        Calendar date = DateHelpers.getFutureDate(30);
        getTextBoxByLabel(page, "Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").fill(String.valueOf(date.get(Calendar.YEAR)));
        getTextBoxByLabel(page, "Start time (24hr format)").fill("10:10");
        getRadioButtonByLabel(page, "Morning").click();
        getRadioButtonByLabel(page, "No").last().click();
        clickButton(page, "Continue");

        // Fill Remote hearing information form
        assertThat(page.locator("h1"))
            .hasText("Remote hearing information", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Video call link (Optional)").fill("http://localhost:3000");
        getTextBoxByLabel(page, "Conference call number (Optional)").fill("01762534632");
        clickButton(page, "Continue");

        // Fill Other information form
        assertThat(page.locator("h1"))
            .hasText("Other information", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Other important information (Optional)")
            .fill("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        clickButton(page, "Continue");

        // Fill Notify parties form
        fillNotifyPartiesForm();

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Listing record created", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(AwaitingHearing.label, newCase.getCaseStatus());
    }

    public void editListing(String... args) {
        Case newCase = new Case(page);
        newCase.startNextStepAction("Hearings: Edit listing");

        // Fill hearing type and format form
        assertThat(page.locator("h1"))
            .hasText("Hearing type and format", textOptionsWithTimeout(60000));
        assertThat(getRadioButtonByLabel(page, "Case management")).isChecked();
        assertThat(getRadioButtonByLabel(page, "Face to Face")).isChecked();
        getRadioButtonByLabel(page, "Interlocutory").check();
        getRadioButtonByLabel(page,"Video").check();
        PageHelpers.clickButton(page, "Continue");

        // Fill Region data form
        assertThat(page.locator("h1"))
            .hasText("Region Data", textOptionsWithTimeout(30000));
        page.selectOption("#recordRegionList", new SelectOption().setLabel("1-London"));
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing location and duration form
        assertThat(page.locator("h1"))
            .hasText("Hearing location and duration", textOptionsWithTimeout(30000));

        List<String> options = Arrays.stream(args)
            .map(arg -> arg.replace(" ", "").toLowerCase()).toList();
        if (options.contains("entervenue")) {
            getCheckBoxByLabel(page, "Venue not listed").check();
            getTextBoxByLabel(page, "Hearing Venue").last()
                .fill("Hendon Magistrates Court, The Court House, The Hyde");
        } else {
            page.selectOption("#recordHearingVenues",
                new SelectOption().setLabel("CROYDON MAGISTRATES COURT-BARCLAY ROAD"));
        }
        getTextBoxByLabel(page, "Room at venue (Optional)").fill("The Court Room 2");
        getTextBoxByLabel(page, "Additional instructions and directions (Optional)")
            .fill("Updated Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        Calendar date = DateHelpers.getFutureDate(45);
        getTextBoxByLabel(page, "Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").fill(String.valueOf(date.get(Calendar.YEAR)));
        getTextBoxByLabel(page, "Start time (24hr format)").fill("10:10");
        getRadioButtonByLabel(page, "Morning").click();
        getRadioButtonByLabel(page, "No").last().click();
        clickButton(page, "Continue");

        // Fill Remote hearing information form
        assertThat(page.locator("h1"))
            .hasText("Remote hearing information", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Video call link (Optional)").fill("http://localhost:3001");
        getTextBoxByLabel(page, "Conference call number (Optional)").fill("01762534634");
        clickButton(page, "Continue");

        // Fill Other information form
        assertThat(page.locator("h1"))
            .hasText("Other information", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Other important information (Optional)")
            .fill("Updated Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        clickButton(page, "Continue");

        // Fill Reason for listing change screen
        assertThat(page.locator("h1"))
            .hasText("Reason for listing change", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Why is this listing record being changed?")
            .fill("Listing record is being changed for testing purpose");
        clickButton(page, "Continue");

        // Fill Notify parties form
        fillNotifyPartiesForm();

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Listing record updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(AwaitingHearing.label, newCase.getCaseStatus());
    }

    public void createHearingSummary() {
        Case newCase = new Case(page);
        newCase.startNextStepAction("Hearings: Create summary");

        // Fill Select hearing form
        selectHearing();

        // Fill hearing type and format form
        assertThat(page.locator("h1"))
            .hasText("Hearing type and format", textOptionsWithTimeout(30000));
        assertThat(getRadioButtonByLabel(page, "Case management")).isChecked();
        assertThat(getRadioButtonByLabel(page, "Face to Face")).isChecked();
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing location and duration form
        assertThat(page.locator("h1"))
            .hasText("Hearing location and duration", textOptionsWithTimeout(30000));
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing attendees form
        assertThat(page.locator("h1"))
            .hasText("Hearing attendees", textOptionsWithTimeout(30000));
        page.selectOption("#hearingSummaryJudge", new SelectOption().setLabel("Chetan Lad"));
        getRadioButtonByLabel(page, "Yes").click();
        page.selectOption("#hearingSummaryPanelMemberList_0_name", new SelectOption().setLabel("Ivy-Rose Rayner"));
        getRadioButtonByLabel(page, "Full member").click();
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing attendees second form
        assertThat(page.locator("h1"))
            .hasText("Hearing attendees", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Appellant").first().check();
        getCheckBoxByLabel(page, "Observer").check();
        getCheckBoxByLabel(page, "Other").check();
        getTextBoxByLabel(page, "Who was this other attendee?").fill("Special officer");
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing outcome form
        assertThat(page.locator("h1"))
            .hasText("Hearing outcome", textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, "Adjourned").click();
        PageHelpers.clickButton(page, "Continue");

        // Fill Upload hearing recording form
        assertThat(page.locator("h1"))
            .hasText("Upload hearing recording", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Where can the recording be found? (Optional)").fill("http://localhost:3000");
        PageHelpers.clickButton(page, "Continue");

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Hearing summary created", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(AwaitingOutcome.label, newCase.getCaseStatus());
    }

    public void editHearingSummary() {
        Case newCase = new Case(page);
        newCase.startNextStepAction("Hearings: Edit summary");

        // Fill select hearing form
        assertThat(page.locator("h1"))
            .hasText("Select hearing summary", textOptionsWithTimeout(60000));
        page.selectOption("#cicCaseHearingSummaryList", new SelectOption().setIndex(1));
        PageHelpers.clickButton(page, "Continue");

        // Fill hearing type and format form
        assertThat(page.locator("h1"))
            .hasText("Hearing type and format", textOptionsWithTimeout(30000));
        assertThat(getRadioButtonByLabel(page, "Case management")).isChecked();
        assertThat(getRadioButtonByLabel(page, "Face to Face")).isChecked();
        getRadioButtonByLabel(page, "Final").click();
        getRadioButtonByLabel(page, "Hybrid").click();
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing location and duration form
        assertThat(page.locator("h1"))
            .hasText("Hearing location and duration", textOptionsWithTimeout(30000));
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing attendees form
        assertThat(page.locator("h1"))
            .hasText("Hearing attendees", textOptionsWithTimeout(30000));
        page.selectOption("#hearingSummaryJudge", new SelectOption().setLabel("Chetan Lad"));
        getRadioButtonByLabel(page, "Yes").click();
        page.selectOption("#hearingSummaryPanelMemberList_0_name", new SelectOption().setLabel("Ivy-Rose Rayner"));
        getRadioButtonByLabel(page, "Observer").click();
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing attendees second form
        assertThat(page.locator("h1"))
            .hasText("Hearing attendees", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Appellant").first().check();
        getCheckBoxByLabel(page, "Observer").check();
        getCheckBoxByLabel(page, "Other").check();
        getTextBoxByLabel(page, "Who was this other attendee?").fill("Special officer");
        PageHelpers.clickButton(page, "Continue");

        // Fill Hearing outcome form
        assertThat(page.locator("h1"))
            .hasText("Hearing outcome", textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, "Adjourned").click();
        PageHelpers.clickButton(page, "Continue");

        // Fill Upload hearing recording form
        assertThat(page.locator("h1"))
            .hasText("Upload hearing recording", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Where can the recording be found? (Optional)").fill("http://localhost:3000");
        PageHelpers.clickButton(page, "Continue");

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Hearing summary edited", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(AwaitingOutcome.label, newCase.getCaseStatus());
    }

    public void cancelHearing() {
        Case newCase = new Case(page);
        newCase.startNextStepAction("Hearings: Cancel hearing");

        // Fill Select hearing form
        selectHearing();

        // Fill Reasons for cancellation form
        assertThat(page.locator("h1"))
            .hasText("Reasons for cancellation", textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, "Incomplete Panel").click();
        getTextBoxByLabel(page, "Enter any other important information about this cancellation (Optional)")
            .fill("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        clickButton(page, "Continue");

        // Fill Notify parties form
        fillNotifyPartiesForm();

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        String cancelReason = getValueFromTableFor(page, "Why was the hearing cancelled?");
        Assertions.assertEquals("Incomplete Panel", cancelReason);
        clickButton(page, "Save and continue");

        // Hearing created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Hearing cancelled", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    public void postponeHearing() {
        Case newCase = new Case(page);
        newCase.startNextStepAction("Hearings: Postpone hearing");

        // Fill Select hearing form
        selectHearing();

        // Fill Reasons for postponement form
        assertThat(page.locator("h1"))
            .hasText("Reasons for postponement", textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, "Appellant not ready to proceed").click();
        getTextBoxByLabel(page, "Enter any other important information about this postponement (Optional)")
            .fill("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        clickButton(page, "Continue");

        // Fill Notify parties form
        fillNotifyPartiesForm();

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        String postponeReason = getValueFromTableFor(page, "Why was the hearing postponed?");
        Assertions.assertEquals("Appellant not ready to proceed", postponeReason);
        clickButton(page, "Save and continue");

        // Hearing postponed confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Hearing Postponed", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    private void selectHearing() {
        assertThat(page.locator("h1"))
            .hasText("Select hearing", textOptionsWithTimeout(60000));
        page.selectOption("#cicCaseHearingList", new SelectOption().setIndex(1));
        PageHelpers.clickButton(page, "Continue");
    }

    private void fillNotifyPartiesForm() {
        assertThat(page.locator("h1"))
            .hasText("Notify parties", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Subject").check();
        if (page.isVisible("#cicCaseNotifyPartyRepresentative-RepresentativeCIC")) {
            getCheckBoxByLabel(page, "Representative").check();
        }
        getCheckBoxByLabel(page, "Respondent").check();
        clickButton(page, "Continue");
    }
}
