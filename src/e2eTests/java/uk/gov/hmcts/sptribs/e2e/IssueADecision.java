package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;


public class IssueADecision extends Base {
    private Page page;

    @Test
    public void caseWorkerShouldIssueADecision() {
        page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        Hearing hearing = new Hearing(page);
        hearing.createListing();
        hearing.createHearingSummary();
        newCase.startNextStepAction("Decision: Issue a decision");
        assertThat(page.locator("h1"))
            .hasText("Create a decision notice", textOptionsWithTimeout(60000));
        page.getByLabel("Create from a template").check();
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Select a template", textOptionsWithTimeout(60000));
        page.selectOption("#caseIssueDecisionIssueDecisionTemplate", new SelectOption().setLabel("CIC7 - ME Dmi Reports"));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Edit Decision", textOptionsWithTimeout(60000));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Document footer", textOptionsWithTimeout(60000));
        getTextBoxByLabel(page, "Decision notice signature").fill("judge test " + StringHelpers.getRandomString(9));
        //getTextBoxByLabel("Decision notice signature").fill("judge test" + StringHelpers.getRandomString(9));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Decision notice preview", textOptionsWithTimeout(60000));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h1"))
            .hasText("Select recipients", textOptionsWithTimeout(60000));
        page.getByLabel("Subject").check();
        page.getByLabel("Representative").check();
        page.getByLabel("Respondent").check();
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("h1").locator("nth=1"))
            .hasText("Decision notice issued", textOptionsWithTimeout(30000));
        assertThat(page.locator("h2"))
            .hasText("A notification has been sent to: Subject, Respondent, Representative", textOptionsWithTimeout(30000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Case management", newCase.getCaseStatus());
    }
}
