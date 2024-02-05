package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Order;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.INCOMPATIBLE_REFERRAL_REASON;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ReferCaseToLegalOfficer;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class ReferCaseToLegalOfficerTests extends Base {

    @Order(1)
    @RepeatedIfExceptionsTest
    void referCaseToLegalOfficerTest() {
        Page page = getPage();
        startReferCaseToLegalOfficerJourney(page);
        assertThat(page.locator("h1")).hasText("Refer case to legal officer", textOptionsWithTimeout(60000));
        page.selectOption("#referToLegalOfficerReferralReason", "Other");
        getTextBoxByLabel(page, "Reason for referral").fill("TEST");
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Additional information", textOptionsWithTimeout(60000));
        getTextBoxByLabel(page, "Provide additional details (Optional)").fill("Test additional details");
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2")).hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Referral completed", textOptionsWithTimeout(30000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Case newCase = new Case(page);
        assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    @Order(2)
    @RepeatedIfExceptionsTest
    public void errorInvalidCaseStatusReferCaseToLegalOfficer() {
        Page page = getPage();
        startReferCaseToLegalOfficerJourney(page);
        page.selectOption("#referToLegalOfficerReferralReason", "Reinstatement request");
        clickButton(page, "Continue");
        assertThat(page.locator("#errors li"))
            .hasText(INCOMPATIBLE_REFERRAL_REASON, textOptionsWithTimeout(30000));
    }

    private void startReferCaseToLegalOfficerJourney(Page page) {
        Login login = new Login(page);
        login.loginAsHearingCentreTL();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.startNextStepAction(ReferCaseToLegalOfficer);
        assertThat(page.locator("h1")).hasText(ReferCaseToLegalOfficer.label, textOptionsWithTimeout(60000));
    }
}
