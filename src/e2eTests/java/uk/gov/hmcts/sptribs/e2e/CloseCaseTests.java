package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import uk.gov.hmcts.sptribs.e2e.enums.Actions;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;

import java.util.Calendar;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ReinstateCase;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseClosed;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class CloseCaseTests extends Base {


    @Order(1)
    @RepeatedIfExceptionsTest
    public void caseworkerShouldAbleToCloseTheCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.startNextStepAction(Actions.CloseCase);
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
        Assertions.assertEquals(CaseClosed.label, newCase.getCaseStatus());
    }

    @Disabled
    public void caseworkerShouldAbleToReinstateCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.caseworkerShouldAbleToCloseTheCase();
        newCase.startNextStepAction(ReinstateCase);
        assertThat(page.locator("h1")).hasText("Are you sure you want to reinstate this case?", textOptionsWithTimeout(60000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Reason for reinstatement", textOptionsWithTimeout(30000));
        page.getByLabel("Request following a withdrawal decision").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Upload documents", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Contact parties", textOptionsWithTimeout(30000));
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case reinstated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }
}



