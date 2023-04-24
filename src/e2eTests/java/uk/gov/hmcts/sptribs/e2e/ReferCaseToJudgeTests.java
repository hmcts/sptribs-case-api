package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.sptribs.e2e.enums.Actions;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;


public class ReferCaseToJudgeTests extends Base {

    private Page page;
    private Case newCase;

    @BeforeEach
    void startReferCaseToJudgeJourney() {
        page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.startNextStepAction(Actions.ReferCaseToJudge);
        assertThat(page.locator("h1")).hasText(Actions.ReferCaseToJudge.label, textOptionsWithTimeout(60000));
    }

    @RepeatedIfExceptionsTest
    public void caseworkerShouldAbleToReferCaseToJudge() {
        page.selectOption("#referToJudgeReferralReason",
            new SelectOption().setLabel("Time extension request"));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Additional information", textOptionsWithTimeout(60000));
        getTextBoxByLabel(page, "Provide additional details (Optional)").fill("test additional details");
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(60000));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Referral completed", textOptionsWithTimeout(30000));
        //assertThat(page.locator("h1")).hasText("Referral completed", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    @RepeatedIfExceptionsTest
    public void errorInvalidCaseStatusReferCaseToJudge() {
        page.selectOption("#referToJudgeReferralReason",
            new SelectOption().setLabel("Reinstatement request"));
        PageHelpers.clickButton(page, "Continue");
        assertThat(page.locator("#errors li"))
            .hasText("The case state is incompatible with the selected referral reason", textOptionsWithTimeout(30000));
    }
}

