package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.System.getenv;
import static uk.gov.hmcts.sptribs.e2e.Base.DSS_AAT_URL;
import static uk.gov.hmcts.sptribs.e2e.Base.DSS_BASE_URL;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.visibleOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getRadioButtonByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class DssCase {
    private final Page page;

    public DssCase(Page page) {
        this.page = page;
    }

    public void navigateToDssLoginPage() {
        DSS_BASE_URL = getenv("DSS_BASE_URL") == null ? DSS_AAT_URL : getenv("DSS_BASE_URL");
        page.navigate(DSS_BASE_URL, new Page.NavigateOptions().setTimeout(120000));
        assertH1Heading(page, "Submit a First-tier Tribunal form");
        clickButton(page, "Start now");
    }

    /*
    createCase() method accepts String arguments. By default, this method creates
    case with Subject details. To include representative details for the case or answer
    PCQ questionnaire use it as below:

    createCase("representative") --> to add representative details to the case
    createCase("pcq") --> to answer pcq questionnaire
    createCase("representative", "pcq") --> for both
    */

    public String createCase(String... args) {

        //Fill subject details
        assertH1Heading(page, "Who is the subject of this case?");
        getTextBoxByLabel(page, "Full name").fill("Subject " + StringHelpers.getRandomString(9));
        getTextBoxByLabel(page, "Day").fill("1");
        getTextBoxByLabel(page, "Month").fill("1");
        getTextBoxByLabel(page, "Year").type("1990");
        clickButton(page, "Continue");
        assertH1Heading(page, "Enter contact information");
        getTextBoxByLabel(page, "Email address")
            .fill(StringHelpers.getRandomString(9).toLowerCase() + "@sub.com");
        getTextBoxByLabel(page, "Contact number")
            .fill("07465730332");
        getCheckBoxByLabel(page, "I agree to be contacted about this case using the details I have provided").click();
        clickButton(page, "Continue");

        assertH1Heading(page, "Is there a representative named on completed tribunal form?");
        List<String> options = Arrays.stream(args).map(String::toLowerCase).toList();

        if (options.contains("representative")) {
            getRadioButtonByLabel(page, "Yes").click();
            clickButton(page, "Continue");
            assertH1Heading(page, "Is the named representative legally qualified?");
            getRadioButtonByLabel(page, "Yes").click();
            clickButton(page, "Continue");

            //Fill representative details
            assertH1Heading(page, "Representative's Details");
            getTextBoxByLabel(page, "Full name").fill("Representative " + StringHelpers.getRandomString(9));
            getTextBoxByLabel(page, "Organisation or business name").fill("Organisation " + StringHelpers.getRandomString(9));
            getTextBoxByLabel(page, "Contact number").fill("07353776541");
            getTextBoxByLabel(page, "Email address").fill(StringHelpers.getRandomString(9).toLowerCase() + "@rep.com");
        } else {
            getRadioButtonByLabel(page, "No").click();
        }
        clickButton(page,"Continue");

        //Upload tribunal form
        assertH1Heading(page, "Upload tribunal form");
        uploadDocument(page);
        clickButton(page,"Continue");

        //Upload supporting documents
        assertH1Heading(page, "Upload supporting documents");
        uploadDocument(page);
        clickButton(page,"Continue");

        //Fill additional information
        assertH1Heading(page, "Add information to a case");
        uploadDocument(page);
        page.locator("#documentRelevance").fill("This is a test document");
        page.locator("textarea#additionalInformation").fill("Lorem Ipsum is simply dummy text of the printing and typesetting "
            + "industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, "
            + "when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        clickButton(page,"Continue");

        //PCQ questions
        String pcqQuestionsAction = options.contains("pcq") ? "answer" : "do not answer";
        answerPcqQuestions(page, pcqQuestionsAction);

        //Verify check answers summary
        assertH1Heading(page, "Check your answers before submitting your tribunal form");
        assertThat(page.locator("h2:has-text(\"Subject Details\")")).isVisible(visibleOptionsWithTimeout(30000));
        clickButton(page,"Accept and send");

        //Tribunal form sent confirmation
        assertH1Heading(page, "Tribunal form sent");

        String caseNumberHeading = page.locator("div.govuk-panel--confirmation div strong").textContent();
        return caseNumberHeading.split(":")[1].replaceAll(" ", "");
    }

    private void answerPcqQuestions(Page page, String action) {
        assertThat(page.locator("h1:has-text(\"Equality and diversity questions\")")).isVisible(visibleOptionsWithTimeout(60000));
        if (page.isVisible("button.cookie-banner-accept-button")) {
            clickButton(page, "Accept additional cookies");
            clickButton(page, "Hide this message");
        }
        if (action.equalsIgnoreCase("answer")) {
            clickButton(page, "Continue to the questions");
            assertH1Heading(page, "What is your main language?");
            getRadioButtonByLabel(page, "English or Welsh").click();
            clickButton(page, "Continue");

            assertH1Heading(page, "What is your sex?");
            getRadioButtonByLabel(page, "Male").first().click();
            clickButton(page, "Continue");

            assertH1Heading(page, "Is your gender the same as the sex you were registered at birth?");
            getRadioButtonByLabel(page, "Yes").click();
            clickButton(page, "Continue");

            assertH1Heading(page, "Which of the following best describes how you think of yourself?");
            getRadioButtonByLabel(page, "Heterosexual or straight").click();
            clickButton(page, "Continue");

            assertH1Heading(page, "Are you married or in a legally registered civil partnership?");
            getRadioButtonByLabel(page, "Yes").click();
            clickButton(page, "Continue");

            assertH1Heading(page, "What is your ethnic group?");
            getRadioButtonByLabel(page, "Prefer not to say").click();
            clickButton(page, "Continue");

            assertH1Heading(page, "What is your religion?");
            getRadioButtonByLabel(page, "Prefer not to say").click();
            clickButton(page, "Continue");

            assertH1Heading(page, "Do you have any physical or mental health conditions or illnesses lasting "
                + "or expected to last 12 months or more?");
            getRadioButtonByLabel(page, "No").first().click();
            clickButton(page, "Continue");

            assertH1Heading(page, "Are you pregnant or have you been pregnant in the last year?");
            getRadioButtonByLabel(page, "No").first().click();
            clickButton(page, "Continue");

            assertH1Heading(page, "You have answered the equality questions");
            clickButton(page, "Continue to the next steps");
        } else {
            clickButton(page, "I don't want to answer these questions");
        }
    }

    private void assertH1Heading(Page page, String heading) {
        assertThat(page.locator("h1").first())
            .hasText(heading, textOptionsWithTimeout(60000));
    }

    private void uploadDocument(Page page) {
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/sample_file.pdf"));
        clickButton(page, "Upload file");
        page.waitForSelector("#filesUploaded li", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    }
}
