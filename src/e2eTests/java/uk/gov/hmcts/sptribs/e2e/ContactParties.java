package uk.gov.hmcts.sptribs.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;

public class ContactParties extends Base {
    @Test
    @Order(1)
    public void caseWorkerShouldBeAbleToConnectContactParties() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase("representative");
        newCase.buildCase();
        newCase.startNextStepAction("Case: Contact parties");
        assertThat(page.locator("h1")).hasText("Which parties do you want to contact?", textOptionsWithTimeout(30000));
        page.getByLabel("Subject").check();
        page.getByLabel("Representative").check();
        page.getByLabel("Respondent").check();
        page.getByLabel("Message").click();
        page.getByLabel("Message").fill("test123");
        clickButton("Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton("Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Message sent", textOptionsWithTimeout(60000));
        clickButton("Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals("Case management", newCase.getCaseStatus());
    }
}

