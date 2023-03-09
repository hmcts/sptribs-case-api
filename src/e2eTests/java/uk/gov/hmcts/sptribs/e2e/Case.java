package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.e2e.enums.Actions;
import uk.gov.hmcts.sptribs.e2e.enums.CaseState;
import uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CreateDraft;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.SendOrder;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseClosed;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseStayed;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.Submitted;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.containsTextOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.functionOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getRadioButtonByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.selectorOptionsWithTimeout;

public class Case {

    public static final String SELECT_A_VALUE = "--Select a value--";
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

        List<String> options = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toList());
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
                .hasText("Who is the applicant in this case?", textOptionsWithTimeout(60000));
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
                .hasText("Who is the Representative for this case?", textOptionsWithTimeout(60000));
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
        Assertions.assertEquals(Submitted.label, getCaseStatus());

        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        return page.locator("ccd-markdown markdown h3")
            .textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
    }

    public void buildCase() {
        startNextStepAction("Case: Build case");
        assertThat(page.locator("h1"))
            .hasText("Case Built", textOptionsWithTimeout(60000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Case: Build case", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("h1").last())
            .hasText("Case built successful", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, getCaseStatus());
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
        assertThat(page.locator("h4")).containsText(CaseStayed.label);
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
        Assertions.assertEquals(CaseManagement.label, getCaseStatus());
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
        Assertions.assertEquals(CaseClosed.label, getCaseStatus());
    }

    public boolean testChangeStateTo(CaseState state) {
        startNextStepAction("Test change state");
        assertThat(page.locator("h1")).hasText("Test change state", textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, state.label).click();
        clickButton(page, "Continue");
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("State changed", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        return true;
    }

    public void createDraft(DraftOrderTemplate template) {
        startNextStepAction(CreateDraft.label);
        assertThat(page.locator("h1"))
            .hasText("Create order", textOptionsWithTimeout(60000));
        page.selectOption("#orderContentOrderTemplate",
            new SelectOption().setLabel(template.label));
        clickButton(page, "Continue");
        getTextBoxByLabel(page, "Main content").type("  +++  This is the main content area");
        clickButton(page, "Continue");
        getTextBoxByLabel(page, "Order signature").fill("Tribunal Judge Farrelly");
        clickButton(page, "Continue");
        assertThat(page.locator("a.ng-star-inserted").last())
            .containsText(" Order-[Subject", containsTextOptionsWithTimeout(60000));
        assertThat(page.locator("a.ng-star-inserted").last())
            .containsText(".pdf", containsTextOptionsWithTimeout(60000));
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Draft order created.", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertThat(page.locator("tr a").first())
            .hasText(CreateDraft.label, textOptionsWithTimeout(60000));
    }

    public void sendOrder() {
        startNextStepAction(SendOrder);
        getRadioButtonByLabel(page, "Issue and send an existing draft").click();
        clickButton(page, "Continue");
        assertThat(page.locator("#cicCaseDraftOrderDynamicList option").first())
            .hasText(SELECT_A_VALUE);
        Assertions.assertTrue(page.locator("#cicCaseDraftOrderDynamicList option").count() > 1);
        String draftName = page.locator("#cicCaseDraftOrderDynamicList option").allTextContents().get(1);
        page.selectOption("#cicCaseDraftOrderDynamicList", draftName);
        clickButton(page, "Continue");
        clickButton(page, "Add new");
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel(page, "Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").type(String.valueOf(date.get(Calendar.YEAR)));
        getTextBoxByLabel(page, "Due Date information (Optional)").type("OPTIONAL Due date information populated by automation framework");
        getCheckBoxByLabel(page, "Yes").check();
        clickButton(page, "Continue");
        getCheckBoxByLabel(page, "Subject").check();
        getCheckBoxByLabel(page, "Respondent").check();
        clickButton(page, "Continue");
        getRadioButtonByLabel(page, "Yes").click();
        getRadioButtonByLabel(page, "7 days").click();
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Order sent", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertThat(page.locator("tr a").first())
            .hasText(SendOrder.label, textOptionsWithTimeout(60000));
    }
}
