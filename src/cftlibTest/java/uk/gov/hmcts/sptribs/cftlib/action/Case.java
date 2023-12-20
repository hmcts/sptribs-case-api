package uk.gov.hmcts.sptribs.cftlib.action;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.cftlib.util.DateHelpers;
import uk.gov.hmcts.sptribs.cftlib.util.PageHelpers;
import uk.gov.hmcts.sptribs.cftlib.util.StringHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.cftlib.util.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.functionOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.getRadioButtonByLabel;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.getTextBoxByLabel;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.selectorOptionsWithTimeout;

public class Case {

    private final Page page;

    public Case(Page page) {
        this.page = page;
    }

    /*
    createCase() method accepts String arguments. By default, this method creates
    case with Subject and email as contact preference. To create case with
    applicant and/or representative you specify the party and/or contact preference
    as the arguments. Examples below:

    createCase("applicant") --> to add applicant to the case
    createCase("representative") --> to add representative to the case
    createCase("applicant", "representative") --> to add applicant and representative to the case
    createCase("post") --> to change the contact preference type to Post for all the parties involved
     */
    public String createCase(String... args) {

        // Select case filters
        clickLink(page, "Create case");
        assertThat(page.locator("h1"))
            .hasText("Create Case", textOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 1",
            "#cc-jurisdiction", functionOptionsWithTimeout(60000));
        page.selectOption("#cc-jurisdiction", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-case-type", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-event", new SelectOption().setLabel("Create Case"));
        page.locator("ccd-create-case-filters button[type='submit']").click();
        clickButton(page, "Start");

        // Select case categories
        assertThat(page.locator("h1"))
            .hasText("Case categorisation", textOptionsWithTimeout(30000));
        page.selectOption("#cicCaseCaseCategory", new SelectOption().setLabel("Assessment")); // Available options: Assessment, Eligibility
        page.selectOption("#cicCaseCaseSubcategory", new SelectOption().setLabel("Fatal")); // Available options: Fatal, Sexual Abuse, Other
        clickButton(page, "Continue");

        // Fill date case received form
        assertThat(page.locator("h1"))
            .hasText("When was the case received?", textOptionsWithTimeout(30000));
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel(page, "Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").type(String.valueOf(date.get(Calendar.YEAR)));
        clickButton(page, "Continue");

        // Fill identified parties form
        assertThat(page.locator("h1"))
            .hasText("Who are the parties in this case?", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Subject").first().check();

        List<String> options = Arrays.stream(args).map(String::toLowerCase).toList();
        if (options.contains("representative")) {
            getCheckBoxByLabel(page, "Representative").check();
        }
        if (options.contains("applicant")) {
            getCheckBoxByLabel(page, "Applicant (if different from subject)").check();
        }
        clickButton(page, "Continue");

        // Fill subject details form
        assertThat(page.locator("h1"))
            .hasText("Who is the subject of this case?", textOptionsWithTimeout(30000));
        getTextBoxByLabel(page, "Subject's full name").fill("Subject " + StringHelpers.getRandomString(9));
        getTextBoxByLabel(page, "Subject's phone number (Optional)").fill("07465730467");
        getTextBoxByLabel(page, "Day").fill("1");
        getTextBoxByLabel(page, "Month").fill("1");
        getTextBoxByLabel(page, "Year").fill("1990");
        page.locator("text = What is subject's contact preference type?").click();
        if (options.contains("post")) {
            getRadioButtonByLabel(page, "Post").click();
        } else {
            getRadioButtonByLabel(page, "Email").click();
            getTextBoxByLabel(page, "Subject's email address")
                .fill(StringHelpers.getRandomString(9).toLowerCase() + "@sub.com");
        }
        fillAddressDetails("Subject");
        clickButton(page, "Continue");

        // Fill applicant details form
        if (options.contains("applicant")) {
            assertThat(page.locator("h1"))
                .hasText("Who is the applicant in this case?", textOptionsWithTimeout(30000));
            getTextBoxByLabel(page, "Applicant's full name").fill("Applicant " + StringHelpers.getRandomString(9));
            getTextBoxByLabel(page, "Applicant's phone number").fill("07465730467");
            getTextBoxByLabel(page, "Day").fill("3");
            getTextBoxByLabel(page, "Month").fill("10");
            getTextBoxByLabel(page, "Year").fill("1992");
            page.locator("text = What is applicant's contact preference?").click();
            if (options.contains("post")) {
                getRadioButtonByLabel(page, "Post").click();
                fillAddressDetails("Applicant");
            } else {
                getRadioButtonByLabel(page, "Email").click();
                getTextBoxByLabel(page, "Applicant's email address")
                    .fill(StringHelpers.getRandomString(9).toLowerCase() + "@applicant.com");
            }
            clickButton(page, "Continue");
        }

        // Fill representative details form
        if (options.contains("representative")) {
            assertThat(page.locator("h1"))
                .hasText("Who is the Representative for this case?", textOptionsWithTimeout(30000));
            getTextBoxByLabel(page, "Representative's full name").fill("Representative " + StringHelpers.getRandomString(9));
            getTextBoxByLabel(page, "Organisation or business name (Optional)").fill(StringHelpers.getRandomString(10) + " Limited");
            getTextBoxByLabel(page, "Representative's contact number").fill("07456831774");
            getTextBoxByLabel(page, "Representative's reference (Optional)").fill(StringHelpers.getRandomString(10));
            getRadioButtonByLabel(page, "Yes").click();
            page.locator("text = What is representative's contact preference?").click();
            if (options.contains("post")) {
                getRadioButtonByLabel(page, "Post").click();
                fillAddressDetails("Representative");
            } else {
                getRadioButtonByLabel(page, "Email").click();
                getTextBoxByLabel(page, "Representative's email address")
                    .fill(StringHelpers.getRandomString(9).toLowerCase() + "@representative.com");
            }
            clickButton(page, "Continue");
        }

        // Fill contact preferences form
        assertThat(page.locator("h1"))
            .hasText("Who should receive information about the case?", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Subject").first().check();
        if (page.isVisible("#cicCaseApplicantCIC-ApplicantCIC")) {
            getCheckBoxByLabel(page, "Applicant (if different from subject)").check();
        }
        if (page.isVisible("#cicCaseRepresentativeCIC-RepresentativeCIC")) {
            getCheckBoxByLabel(page, "Representative").check();
        }
        clickButton(page, "Continue");

        // Upload tribunals form
        assertThat(page.locator("h1"))
            .hasText("Upload tribunal forms", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");

        // Fill further details form
        assertThat(page.locator("h1"))
            .hasText("Enter further details about this case", textOptionsWithTimeout(30000));
        page.selectOption("#cicCaseSchemeCic", new SelectOption().setLabel("2001"));
        page.locator("#cicCaseClaimLinkedToCic_Yes").click();
        getTextBoxByLabel(page, "CICA reference number").fill("CICATestReference");
        page.locator("#cicCaseCompensationClaimLinkCIC_Yes").click();
        getTextBoxByLabel(page, "Police Authority Management Incident").fill("PAMITestReference");
        page.locator("#cicCaseFormReceivedInTime_Yes").click();
        page.locator("#cicCaseMissedTheDeadLineCic_Yes").click();
        clickButton(page, "Continue");

        // Check your answers form
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(30000));
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");

        // Case created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Created", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Submitted", getCaseStatus());

        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        return page.locator("ccd-markdown markdown h3")
            .textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
    }

    public void buildCase() {
        startNextStepAction("Case: Build case");
        assertThat(page.locator("h1"))
            .hasText("Case Built", textOptionsWithTimeout(60000));
        clickButton(page, "Submit");
        assertThat(page.locator("h1").last())
            .hasText("Case built successful", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Case management", getCaseStatus());
    }

    public void editCase() {
        startNextStepAction("Case: Edit case");
        assertThat(page.locator("h1"))
            .hasText("Case categorisation", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("When was the case received?", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Who are the parties in this case?", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Who is the subject of this case?", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Who should receive information about the case?", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Enter further details about this case", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(30000));
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("h1").last())
            .hasText("Case Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
    }

    public String getCaseStatus() {
        getTabByText(page, "State").click();
        page.waitForSelector("h4", PageHelpers.selectorOptionsWithTimeout(60000));
        return page.locator("h4").textContent().split(":")[1].trim();
    }

    public void startNextStepAction(String actionName) {
        page.waitForFunction("selector => document.querySelector(selector).options.length > 1",
            "#next-step", functionOptionsWithTimeout(60000));
        page.selectOption("#next-step", new SelectOption().setLabel(actionName));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(6000));
        page.locator("label:has-text(\"Next step\")").click();
        clickButton(page, "Go");
    }

    public void startNextStepAction(Actions actionName) {
        startNextStepAction(actionName.label);
    }

    public void addStayToCase() {
        startNextStepAction("Stays: Create/edit stay");
        assertThat(page.locator("h1"))
            .hasText("Add a Stay to this case", textOptionsWithTimeout(60000));
        page.getByLabel("Awaiting outcome of civil case").check();
        page.getByLabel("Day").click();
        page.getByLabel("Day").fill("22");
        page.getByLabel("Month").click();
        page.getByLabel("Month").fill("12");
        page.getByLabel("Year").click();
        page.getByLabel("Year").fill("2023");
        page.locator("#stayAdditionalDetail").fill("Additional info for create stay");

        page.locator("button:has-text('Continue')").click();
        page.waitForSelector("h1", PageHelpers.selectorOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Stays: Create/edit stay'",
            "h1", PageHelpers.functionOptionsWithTimeout(60000));

        page.waitForSelector("button:has-text('Save and continue')", PageHelpers.selectorOptionsWithTimeout(60000));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save and continue")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("State")).click();
        page.waitForSelector("h4", PageHelpers.selectorOptionsWithTimeout(60000));
        assertThat(page.locator("h4")).containsText("Case stayed");
    }

    private void fillAddressDetails(String partyType) {
        getTextBoxByLabel(page, "Enter a UK postcode").fill("SW11 1PD");
        clickLink(page, "I can't enter a UK postcode");
        getTextBoxByLabel(page, "Building and Street").fill("1 " + partyType + " Rse Way");
        getTextBoxByLabel(page, "Address Line 2").fill("Test Address Line 2");
        getTextBoxByLabel(page, "Address Line 3").fill("Test Address Line 3");
        getTextBoxByLabel(page, "Town or City").fill("London");
        getTextBoxByLabel(page, "County/State").fill("London");
        getTextBoxByLabel(page, "Country").fill("UK");
        getTextBoxByLabel(page, "Postcode/Zipcode").fill("SW11 1PD");
    }

    public void createCaseFlag() {
        startNextStepAction("Flags: Create flag");
        assertThat(page.locator("h1")).hasText("Flags: Create flag",textOptionsWithTimeout(60000));
        assertThat(page.locator("h2")).hasText("Where should this flag be added?",textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, "Case").click();
        clickButton(page, "Continue");
        getCheckBoxByLabel(page, "Subject").check();
        clickButton(page, "Continue");
        getRadioButtonByLabel(page, "Other").click();
        page.getByLabel("Enter a flag type").click();
        page.getByLabel("Enter a flag type").fill("test flag");
        clickButton(page, "Continue");
        assertThat(page.locator("h2"))
            .hasText("Add comments for this flag (Optional)",textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Flag created", textOptionsWithTimeout(60000));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Case management", getCaseStatus());
    }

    public void caseworkerShouldAbleToCloseTheCase() {
        startNextStepAction("Case: Close case");
        assertThat(page.locator("h1")).hasText("Are you sure you want to close this case?", textOptionsWithTimeout(60000));
        clickButton(page, "Continue");
        page.getByLabel("Case Withdrawn").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Withdrawal details", textOptionsWithTimeout(30000));
        page.getByLabel("Who withdrew from the case?").fill("case worker");
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel(page, "Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").type(String.valueOf(date.get(Calendar.YEAR)));
        page.locator("h1").click();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Upload case documents", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Select recipients", textOptionsWithTimeout(30000));
        page.getByLabel("Subject").check();
        page.getByLabel("Respondent").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case closed", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Case closed", getCaseStatus());
    }

}
