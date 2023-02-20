package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.enabledOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.functionOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getButtonByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getRadioButtonByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.selectorOptionsWithTimeout;

public class Case extends Base {

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
        clickLink("Create case");
        assertThat(page.locator("h1"))
            .hasText("Create Case", textOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 1",
            "#cc-jurisdiction", functionOptionsWithTimeout(60000));
        page.selectOption("#cc-jurisdiction", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-case-type", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-event", new SelectOption().setLabel("Create Case"));
        page.locator("ccd-create-case-filters button[type='submit']").click();
        clickButton("Start");

        // Select case categories
        assertThat(page.locator("h1"))
            .hasText("Case categorisation", textOptionsWithTimeout(30000));
        page.selectOption("#cicCaseCaseCategory", new SelectOption().setLabel("Assessment")); // Available options: Assessment, Eligibility
        page.selectOption("#cicCaseCaseSubcategory", new SelectOption().setLabel("Fatal")); // Available options: Fatal, Sexual Abuse, Other
        clickButton("Continue");

        // Fill date case received form
        assertThat(page.locator("h1"))
            .hasText("When was the case received?", textOptionsWithTimeout(30000));
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel("Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel("Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel("Year").type(String.valueOf(date.get(Calendar.YEAR)));
        clickButton("Continue");

        // Fill identified parties form
        assertThat(page.locator("h1"))
            .hasText("Who are the parties in this case?", textOptionsWithTimeout(30000));
        getCheckBoxByLabel("Subject").first().check();

        List<String> options = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toList());
        if (options.contains("representative")) {
            getCheckBoxByLabel("Representative").check();
        }
        if (options.contains("applicant")) {
            getCheckBoxByLabel("Applicant (if different from subject)").check();
        }
        clickButton("Continue");

        // Fill subject details form
        assertThat(page.locator("h1"))
            .hasText("Who is the subject of this case?", textOptionsWithTimeout(30000));
        getTextBoxByLabel("Subject's full name").fill("Subject " + StringHelpers.getRandomString(9));
        getTextBoxByLabel("Subject's phone number (Optional)").fill("07465730467");
        getTextBoxByLabel("Day").fill("1");
        getTextBoxByLabel("Month").fill("1");
        getTextBoxByLabel("Year").fill("1990");
        page.locator("text = What is subject's contact preference type?").click();
        if (options.contains("post")) {
            getRadioButtonByLabel("Post").click();
            fillAddressDetails("Subject");
        } else {
            getRadioButtonByLabel("Email").click();
            getTextBoxByLabel("Subject's email address")
                        .fill(StringHelpers.getRandomString(9).toLowerCase() + "@sub.com");
        }
        clickButton("Continue");

        // Fill applicant details form
        if (options.contains("applicant")) {
            assertThat(page.locator("h1"))
                .hasText("Who is the applicant in this case?", textOptionsWithTimeout(30000));
            getTextBoxByLabel("Applicant's full name").fill("Subject " + StringHelpers.getRandomString(9));
            getTextBoxByLabel("Applicant's phone number").fill("07465730467");
            getTextBoxByLabel("Day").fill("3");
            getTextBoxByLabel("Month").fill("10");
            getTextBoxByLabel("Year").fill("1992");
            page.locator("text = What is applicant's contact preference?").click();
            if (options.contains("post")) {
                getRadioButtonByLabel("Post").click();
                fillAddressDetails("Applicant");
            } else {
                getRadioButtonByLabel("Email").click();
                getTextBoxByLabel("Applicant's email address")
                    .fill(StringHelpers.getRandomString(9).toLowerCase() + "@applicant.com");
            }
            clickButton("Continue");
        }

        // Fill representative details form
        if (options.contains("representative")) {
            assertThat(page.locator("h1"))
                .hasText("Who is the Representative for this case?", textOptionsWithTimeout(30000));
            getTextBoxByLabel("Representative's full name").fill("Subject " + StringHelpers.getRandomString(9));
            getTextBoxByLabel("Organisation or business name (Optional)").fill(StringHelpers.getRandomString(10) + " Limited");
            getTextBoxByLabel("Representative's contact number").fill("07456831774");
            getTextBoxByLabel("Representative's reference (Optional)").fill(StringHelpers.getRandomString(10));
            getRadioButtonByLabel("Yes").click();
            page.locator("text = What is representative's contact preference?").click();
            if (options.contains("post")) {
                getRadioButtonByLabel("Post").click();
                fillAddressDetails("Representative");
            } else {
                getRadioButtonByLabel("Email").click();
                getTextBoxByLabel("Representative's email address")
                    .fill(StringHelpers.getRandomString(9).toLowerCase() + "@representative.com");
            }
            clickButton("Continue");
        }

        // Fill contact preferences form
        getCheckBoxByLabel("Subject").first().check();
        if (page.isVisible("#cicCaseApplicantCIC-ApplicantCIC")) {
            getCheckBoxByLabel("Applicant (if different from subject)").check();
        }
        if (page.isVisible("#cicCaseRepresentativeCIC-RepresentativeCIC")) {
            getCheckBoxByLabel("Representative").check();
        }
        clickButton("Continue");

        // Upload tribunals form
        assertThat(page.locator("h1"))
            .hasText("Upload tribunal forms", textOptionsWithTimeout(30000));
        clickButton("Continue");

        // Fill further details form
        assertThat(page.locator("h1"))
            .hasText("Enter further details about this case", textOptionsWithTimeout(30000));
        page.selectOption("#cicCaseSchemeCic", new SelectOption().setLabel("2001"));
        page.locator("#cicCaseClaimLinkedToCic_Yes").click();
        getTextBoxByLabel("CICA reference number").fill("CICATestReference");
        page.locator("#cicCaseCompensationClaimLinkCIC_Yes").click();
        getTextBoxByLabel("Police Authority Management Incident").fill("PAMITestReference");
        page.locator("#cicCaseFormReceivedInTime_Yes").click();
        page.locator("#cicCaseMissedTheDeadLineCic_Yes").click();
        clickButton("Continue");

        // Check your answers form
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(20000));
        clickButton("Save and continue");

        // Case created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Created", textOptionsWithTimeout(60000));
        clickButton("Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        return page.locator("ccd-markdown markdown h3")
            .textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
    }

    public void buildCase() {
        page.selectOption("#next-step", new SelectOption().setLabel("Case: Build case"));
        page.locator("ccd-markdown markdown h3").click();
        assertThat(getButtonByText("Go")).isEnabled(enabledOptionsWithTimeout(3000));
        clickButton("Go");
        assertThat(page.locator("h1"))
            .hasText("Case Built", textOptionsWithTimeout(20000));
        clickButton("Continue");
        assertThat(page.locator("h1"))
            .hasText("Case Built", textOptionsWithTimeout(20000));
        clickButton("Save and continue");
        assertThat(page.locator("h1").last())
            .hasText("Case built successful", textOptionsWithTimeout(60000));
        clickButton("Close and Return to case details");
    }

    public void addStayToCase() {
        page.selectOption("#next-step", new SelectOption().setLabel("Stays: Create/edit stay"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(6000));
        PageHelpers.clickButton("Go");
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
        getTextBoxByLabel("Enter a UK postcode").fill("SW11 1PD");
        clickLink("I can't enter a UK postcode");
        getTextBoxByLabel("Building and Street").fill("1 " + partyType + " Rse Way");
        getTextBoxByLabel("Address Line 2").fill("Test Address Line 2");
        getTextBoxByLabel("Address Line 3").fill("Test Address Line 3");
        getTextBoxByLabel("Town or City").fill("London");
        getTextBoxByLabel("County/State").fill("London");
        getTextBoxByLabel("Country").fill("UK");
        getTextBoxByLabel("Postcode/Zipcode").fill("SW11 1PD");
    }

    public void createCaseFlag() {
        page.selectOption("#next-step", new SelectOption().setLabel("Flags: Create flag"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false","ccd-event-trigger button[type='submit']",
            PageHelpers.functionOptionsWithTimeout(6000));
        PageHelpers.clickButton("Go");
        assertThat(page.locator("h1")).hasText("Flags: Create flag",textOptionsWithTimeout(30000));
        assertThat(page.locator("h2")).hasText("Where should this flag be added?",textOptionsWithTimeout(30000));
        page.getByLabel("Case").check();
        clickButton("Continue");
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Subject")).check();
        clickButton("Continue");
        page.getByLabel("Other").check();
        page.getByLabel("Enter a flag type").click();
        page.getByLabel("Enter a flag type").fill("test flag");
        clickButton("Continue");
        clickButton("Continue");
        clickButton("Save and continue");
        assertThat(page.locator("h1:has-text('Case Flag created')")).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
    }
}
