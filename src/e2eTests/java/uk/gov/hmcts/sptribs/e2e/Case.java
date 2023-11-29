package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.e2e.enums.Actions;
import uk.gov.hmcts.sptribs.e2e.enums.CaseParty;
import uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference;
import uk.gov.hmcts.sptribs.e2e.enums.ContactPreference;
import uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.BuildCase;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CloseCase;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CreateDraft;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CreateEditStay;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CreateFlag;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.EditCase;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.LinkCase;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ManageDueDate;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ManageFlags;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ManageLinks;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.SendOrder;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.Subject;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.SubjectEmail;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.SubjectPost;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseClosed;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseStayed;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.Submitted;
import static uk.gov.hmcts.sptribs.e2e.enums.ContactPreference.Post;
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
    case with Subject and post as contact preference. To create case with
    applicant and/or representative you specify the party and/or contact preference
    as the arguments. Examples below:

    Default contact preferences for all the parties is Email
    Subject is the default party that will be added to the case by case-api application, we do not have to add it explicitly
    createCase(CasePartyContactPreference.SubjectPost) --> to add Subject with Post
    createCase(CasePartyContactPreference.Applicant) --> to add applicant to the case
    createCase(CasePartyContactPreference.Representative) --> to add representative to the case
    createCase(CasePartyContactPreference.Applicant, CasePartyContactPreference.Representative) --> to add applicant and rep to the case
    createCase(CasePartyContactPreference.ApplicantPost) --> to add applicant with Post as preference

     */

    public String createCase(CasePartyContactPreference... caseParties) {
        // Select case filters
        page.waitForSelector("xuilib-loading-spinner div.spinner-inner-container",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
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
        page.selectOption("#cicCaseCaseSubcategory", new SelectOption().setLabel("Medical Re-opening"));
        clickButton(page, "Continue");

        // Fill date case received form
        assertThat(page.locator("h1"))
            .hasText("When was the case received?", textOptionsWithTimeout(30000));
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel(page, "Day").fill(valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").type(valueOf(date.get(Calendar.YEAR)));
        clickButton(page, "Continue");

        // Fill identified parties form
        assertThat(page.locator("h1"))
            .hasText("Who are the parties in this case?", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Subject").first().check();

        List<CasePartyContactPreference> casePartyList = new ArrayList<>();
        Collections.addAll(casePartyList, caseParties);

        if (!casePartyList.contains(Subject) && !casePartyList.contains(SubjectPost) && !casePartyList.contains(SubjectEmail)) {
            casePartyList.add(Subject);
        }

        HashMap<CaseParty, ContactPreference> map = new HashMap<>();

        casePartyList.forEach(p -> map.putAll(p.select(page)));

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

        fillAddressDetails("Subject");
        if (map.get(CaseParty.Subject).equals(Post)) {
            getRadioButtonByLabel(page, "Post").click();
        } else {
            getRadioButtonByLabel(page, "Email").click();
            getTextBoxByLabel(page, "Subject's email address")
                .fill(StringHelpers.getRandomString(9).toLowerCase() + "@sub.com");
        }
        clickButton(page, "Continue");

        // Fill applicant details form
        if (map.containsKey(CaseParty.Applicant)) {
            assertThat(page.locator("h1"))
                .hasText("Who is the applicant in this case?", textOptionsWithTimeout(60000));
            getTextBoxByLabel(page, "Applicant's full name").fill("Applicant " + StringHelpers.getRandomString(9));
            getTextBoxByLabel(page, "Applicant's phone number").fill("07465730467");
            getTextBoxByLabel(page, "Day").fill("3");
            getTextBoxByLabel(page, "Month").fill("10");
            getTextBoxByLabel(page, "Year").fill("1992");
            page.locator("text = What is applicant's contact preference?").click();
            setContactPreference(map.get(CaseParty.Applicant).equals(Post), "Applicant", "Applicant's email address", "@applicant.com");
            clickButton(page, "Continue");
        }

        // Fill representative details form
        if (map.containsKey(CaseParty.Representative)) {
            assertThat(page.locator("h1"))
                .hasText("Who is the Representative for this case?", textOptionsWithTimeout(60000));
            getTextBoxByLabel(page, "Representative's full name").fill("Representative " + StringHelpers.getRandomString(9));
            getTextBoxByLabel(page, "Organisation or business name (Optional)").fill(StringHelpers.getRandomString(10) + " Limited");
            getTextBoxByLabel(page, "Representative's contact number").fill("07456831774");
            getTextBoxByLabel(page, "Representative's reference (Optional)").fill(StringHelpers.getRandomString(10));
            getRadioButtonByLabel(page, "Yes").click();
            page.locator("text = What is representative's contact preference?").click();
            setContactPreference(
                map.get(CaseParty.Representative).equals(Post),
                "Representative",
                "Representative's email address",
                "@representative.com"
            );
            clickButton(page, "Continue");
        }

        // Fill contact preferences form
        assertThat(page.locator("h1"))
            .hasText("Who should receive information about the case?", textOptionsWithTimeout(60000));
        page.locator("#cicCaseSubjectCIC-SubjectCIC").check();
        if (page.isVisible("#cicCaseApplicantCIC-ApplicantCIC")) {
            page.locator("#cicCaseApplicantCIC-ApplicantCIC").check();
        }
        if (page.isVisible("#cicCaseRepresentativeCIC-RepresentativeCIC")) {
            page.locator("#cicCaseRepresentativeCIC-RepresentativeCIC").check();
        }
        page.locator("div h1").click();
        clickButton(page, "Continue");

        // Upload tribunals form
        assertThat(page.locator("h1"))
            .hasText("Upload tribunal forms", textOptionsWithTimeout(60000));
        clickButton(page, "Add new");
        String documentCategory = "A - Application Form";
        page.selectOption("#cicCaseApplicantDocumentsUploaded_0_documentCategory", new SelectOption().setLabel(documentCategory));
        String documentName = "sample_file.pdf";
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/" + documentName));
        page.waitForSelector("//span[contains(text(), 'Uploading...')]",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
        page.waitForFunction("selector => document.querySelector(selector).disabled === true",
            "button[aria-label='Cancel upload']", functionOptionsWithTimeout(15000));
        String documentDescription = "This is a test document uploaded during create case journey";
        page.locator("#cicCaseApplicantDocumentsUploaded_0_documentEmailContent").fill(documentDescription);
        clickButton(page, "Continue");

        // Fill further details form
        assertThat(page.locator("h1"))
            .hasText("Enter further details about this case", textOptionsWithTimeout(60000));
        page.selectOption("#cicCaseSchemeCic", new SelectOption().setLabel("2001"));
        page.selectOption("#cicCaseRegionCIC", new SelectOption().setLabel("London"));
        page.locator("#cicCaseClaimLinkedToCic_Yes").click();
        getTextBoxByLabel(page, "CICA reference number").fill("CICATestReference");
        page.locator("#cicCaseCompensationClaimLinkCIC_Yes").click();
        page.locator("#cicCaseFormReceivedInTime_No").click();
        page.locator("#cicCaseMissedTheDeadLineCic_Yes").click();
        clickButton(page, "Continue");

        // Check your answers form
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(30000));
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");

        // Case created confirmation screen
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Created", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(Submitted.label, getCaseStatus());

        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        return page.locator("ccd-markdown markdown h3")
            .textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
    }

    private void setContactPreference(boolean post, String party, String label, String domain) {
        if (post) {
            getRadioButtonByLabel(page, "Post").click();
            fillAddressDetails(party);
        } else {
            getRadioButtonByLabel(page, "Email").click();
            getTextBoxByLabel(page, label)
                .fill(StringHelpers.getRandomString(9).toLowerCase() + domain);
        }
    }

    public String editDssCase(String... args) {
        startNextStepAction(EditCase);
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
        getTextBoxByLabel(page, "Day").fill(valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").type(valueOf(date.get(Calendar.YEAR)));
        clickButton(page, "Continue");

        // Fill identified parties form
        assertThat(page.locator("h1"))
            .hasText("Who are the parties in this case?", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Subject").first().check();

        List<String> options = stream(args).map(String::toLowerCase).toList();
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
        page.locator("text = What is subject's contact preference type?").click();
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
            setContactPreference(options.contains("post"), "Applicant", "Applicant's email address", "@applicant.com");
        }

        clickButton(page, "Continue");

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
            setContactPreference(options.contains("post"), "Representative", "Representative's email address", "@representative.com");
        }

        clickButton(page, "Continue");

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
            .hasText("Case Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        // Case details screen
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(Submitted.label, getCaseStatus());

        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        return page.locator("ccd-markdown markdown h3")
            .textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
    }

    public void buildCase() {
        startNextStepAction(BuildCase);
        assertThat(page.locator("h1"))
            .hasText("Case Built", textOptionsWithTimeout(60000));
        clickButton(page, "Submit");
        assertThat(page.locator("h1").last())
            .hasText("Case built successful", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, getCaseStatus());
    }

    public String getCaseStatus() {
        getTabByText(page, "State").click();
        page.waitForSelector("h4", PageHelpers.selectorOptionsWithTimeout(60000));
        return page.locator("h4").textContent().split(":")[1].trim();
    }

    public void startNextStepAction(Actions actionName) {
        page.waitForFunction("selector => document.querySelector(selector).options.length > 1",
            "#next-step", functionOptionsWithTimeout(60000));
        page.selectOption("#next-step", new SelectOption().setLabel(actionName.label));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(6000));
        page.locator("label:has-text(\"Next step\")").click();
        clickButton(page, "Go");
    }

    public void addStayToCase() {
        startNextStepAction(CreateEditStay);
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

    public void createCaseLevelFlag() {
        createFlag("case level");
    }

    public void createFlagForParties(CaseParty... parties) {
        stream(parties).map(Enum::name).forEach(this::createFlag);
    }

    public void updateCaseLevelFlags() {
        startNextStepAction(ManageFlags);
        assertThat(page.locator("h1").last()).hasText("Manage case flags", textOptionsWithTimeout(60000));
        getRadioButtonByLabel(page, "case level").click();
        clickButton(page, "Next");
        clickButton(page, "Make inactive");
        page.locator("#flagComments").fill("Test Manage case flag, making the flag inactive");
        clickButton(page, "Next");
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Review flag details", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Flag updated", textOptionsWithTimeout(60000));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, getCaseStatus());
    }

    private void createFlag(String flagLocation) {
        startNextStepAction(CreateFlag);
        assertThat(page.locator("h1").last()).hasText("Where should this flag be added?", textOptionsWithTimeout(30000));
        getRadioButtonByLabel(page, flagLocation).click();
        clickButton(page, "Next");
        getRadioButtonByLabel(page, "Other").click();
        page.getByLabel("Enter a flag type").click();
        page.getByLabel("Enter a flag type").fill("test flag");
        clickButton(page, "Next");
        page.locator("#flagComments").fill("Test Flag Comment");
        clickButton(page, "Next");
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Review flag details", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Flag created", textOptionsWithTimeout(60000));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, getCaseStatus());
    }

    public void caseworkerShouldAbleToCloseTheCase() {
        startNextStepAction(CloseCase);
        assertThat(page.locator("h1")).hasText("Are you sure you want to close this case?", textOptionsWithTimeout(60000));
        clickButton(page, "Continue");
        page.getByLabel("Case Withdrawn").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Withdrawal details", textOptionsWithTimeout(30000));
        page.getByLabel("Who withdrew from the case?").fill("case worker");
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel(page, "Day").fill(valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel(page, "Month").fill(valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel(page, "Year").type(valueOf(date.get(Calendar.YEAR)));
        page.locator("h1").click();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Upload case documents", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Select recipients", textOptionsWithTimeout(30000));
        getCheckBoxByLabel(page, "Subject").check();
        getCheckBoxByLabel(page, "Respondent").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case closed", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseClosed.label, getCaseStatus());
    }

    public void createDraft(DraftOrderTemplate template) {
        startNextStepAction(CreateDraft);
        assertThat(page.locator("h1"))
            .hasText("Create order", textOptionsWithTimeout(60000));
        page.selectOption("#orderContentOrderTemplate",
            new SelectOption().setLabel(template.label));
        clickButton(page, "Continue");
        getTextBoxByLabel(page, "Main content").type("  +++  This is the main content area");
        clickButton(page, "Continue");
        getTextBoxByLabel(page, "Order signature").fill("Tribunal Judge Farrelly");
        clickButton(page, "Continue");
        assertThat(page.locator("ccd-read-document-field a.ng-star-inserted"))
            .containsText(" Order--[Subject", containsTextOptionsWithTimeout(60000));
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

    public void selectAnExistingDraftOrderAndSend() {
        selectAnExistingDraftOrderAndSend(now().plusMonths(1));
    }

    public void selectAnExistingDraftOrderAndSend(LocalDate localDate) {
        startNextStepAction(SendOrder);
        selectAnExistingDraft();
        addOrderDueDatesAndCompleteJourney(localDate);
    }

    public void uploadAndSendOrder(LocalDate localDate) {
        startNextStepAction(SendOrder);
        uploadAnewDocument();
        addOrderDueDatesAndCompleteJourney(localDate);
    }

    private void selectAnExistingDraft() {
        getRadioButtonByLabel(page, "Issue and send an existing draft").click();
        clickButton(page, "Continue");
        assertThat(page.locator("#cicCaseDraftOrderDynamicList option").first())
            .hasText(SELECT_A_VALUE);
        Assertions.assertTrue(page.locator("#cicCaseDraftOrderDynamicList option").count() > 1);
        String draftName = page.locator("#cicCaseDraftOrderDynamicList option").allTextContents().get(1);
        page.selectOption("#cicCaseDraftOrderDynamicList", draftName);
        clickButton(page, "Continue");
    }

    private void uploadAnewDocument() {
        getRadioButtonByLabel(page, "Upload a new order from your computer").click();
        clickButton(page, "Continue");
        clickButton(page, "Add new");
        getTextBoxByLabel(page, "Description").fill("PDF file upload");
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/sample_file.pdf"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === true",
            "button[aria-label='Cancel upload']", PageHelpers.functionOptionsWithTimeout(15000));
        clickButton(page, "Continue");
        clickButton(page, "Previous");
        assertThat(page.locator("ccd-read-document-field")).hasText("sample_file.pdf");
        clickButton(page, "Continue");
        assertThat(page.locator("h1.govuk-heading-l")).hasText("Add a due date");
    }

    private void addOrderDueDatesAndCompleteJourney(LocalDate localDate) {
        clickButton(page, "Add new");
        getTextBoxByLabel(page, "Day").fill(valueOf(localDate.getDayOfMonth()));
        getTextBoxByLabel(page, "Month").fill(valueOf(localDate.getMonthValue()));
        getTextBoxByLabel(page, "Year").type(valueOf(localDate.getYear()));
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

    public void mangeOrderDueDateAmendDueDate(LocalDate localDate) {
        startNextStepAction(ManageDueDate);
        assertThat(page.locator("#cicCaseOrderDynamicList option").first())
            .hasText(SELECT_A_VALUE);
        Assertions.assertTrue(page.locator("#cicCaseOrderDynamicList option").count() > 1);
        String draftName = page.locator("#cicCaseOrderDynamicList option").allTextContents().get(1);
        page.selectOption("#cicCaseOrderDynamicList", draftName);
        clickButton(page, "Continue");
        getTextBoxByLabel(page, "Day").fill(String.valueOf(localDate.getDayOfMonth()));
        getTextBoxByLabel(page, "Month").fill(String.valueOf(localDate.getMonthValue()));
        getTextBoxByLabel(page, "Year").fill(String.valueOf(localDate.getYear()));
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Due dates amended.", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertThat(page.locator("tr a").first())
            .hasText(ManageDueDate.label, textOptionsWithTimeout(60000));
    }

    public void linkCase(String caseId1, String caseId2) {
        openCase(caseId1);
        startNextStepAction(LinkCase);
        assertThat(page.locator("h1"))
            .hasText("Before you start", textOptionsWithTimeout(60000));
        clickButton(page, "Next");
        page.locator("#caseNumber input").fill(caseId2);
        page.getByLabel("Bail").check();
        clickButton(page, "Propose case link");
        clickButton(page, "Next");
        clickButton(page, "Submit");
        assertThat(page.locator("h1").last())
            .hasText("Case Link created", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, getCaseStatus());
    }

    public void unlinkCase(String caseId1, String caseId2) {
        openCase(caseId1);
        startNextStepAction(ManageLinks);
        assertThat(page.locator("h1"))
            .hasText("Before you start", textOptionsWithTimeout(60000));
        clickButton(page, "Next");
        page.locator(format("#case-reference-%s", caseId2)).check();
        clickButton(page, "Next");
        clickButton(page, "Submit");
        assertThat(page.locator("h1").last())
            .hasText("Case Link updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, getCaseStatus());
    }

    private void openCase(String caseId1) {
        clickLink(page, "Find Case");
        page.locator("input[id*=CASE_REFERENCE]").fill(caseId1);
        clickButton(page, "Apply");
        page.locator("table tr td").first().click();
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
    }

    public void bundle() {
        startNextStepAction(BuildCase);
        assertThat(page.locator("h1"))
            .hasText("Case Built", textOptionsWithTimeout(60000));
        clickButton(page, "Submit");
        assertThat(page.locator("h1").last())
            .hasText("Case built successful", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, getCaseStatus());
    }
}
