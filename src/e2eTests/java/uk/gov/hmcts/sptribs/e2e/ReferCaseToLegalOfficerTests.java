package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ReferCaseToLegalOfficer;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class ReferCaseToLegalOfficerTests extends Base {
    private Page page;
    private Case newCase;

    @BeforeEach
    void startReferCaseToLegalOfficerJourney() {
        page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.startNextStepAction(ReferCaseToLegalOfficer);
        assertThat(page.locator("h1")).hasText(ReferCaseToLegalOfficer.label, textOptionsWithTimeout(60000));
    }

    @Test
    void referCaseToLegalOfficerTest() {
        assertThat(page.locator("h1")).hasText("Refer case to legal officer", textOptionsWithTimeout(60000));
        page.selectOption("#referToLegalOfficerReferralReason", "Other");
        getTextBoxByLabel(page, "Reason for referral").fill("TEST");
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Additional information", textOptionsWithTimeout(60000));
        getTextBoxByLabel(page, "Provide additional details (Optional)").fill("Test additional details");
        clickButton(page, "Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Referral completed", textOptionsWithTimeout(30000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    @Test
    public void errorInvalidCaseStatusReferCaseToLegalOfficer() {
        page.selectOption("#referToLegalOfficerReferralReason", "Reinstatement request");
        clickButton(page, "Continue");
        assertThat(page.locator("#errors li"))
            .hasText("The case state is incompatible with the selected referral reason", textOptionsWithTimeout(30000));
    }
}
