package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.IssueDecision;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class IssueADecisionTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldIssueADecision() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        Hearing hearing = new Hearing(page);
        hearing.createListing();
        hearing.createHearingSummary();
        newCase.startNextStepAction(IssueDecision);
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
            .hasText("Decision: Issue a decision", textOptionsWithTimeout(60000));
        getCheckBoxByLabel(page, "Subject").check();
        getCheckBoxByLabel(page, "Representative").check();
        getCheckBoxByLabel(page, "Respondent").check();
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Decision notice issued", textOptionsWithTimeout(60000));
        assertThat(page.locator("ccd-markdown markdown h2"))
            .hasText("A notification has been sent to: Subject, Respondent, Representative", textOptionsWithTimeout(30000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }
}
