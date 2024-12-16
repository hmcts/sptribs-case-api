package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import uk.gov.hmcts.sptribs.cftlib.action.Case;
import uk.gov.hmcts.sptribs.cftlib.util.DateHelpers;
import uk.gov.hmcts.sptribs.cftlib.util.Login;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.Calendar;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.cftlib.util.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.getTextBoxByLabel;

public class CaseworkerCloseCaseTest extends XuiTest {

    @RepeatedIfExceptionsTest
    public void caseworkerShouldAbleToCloseTheCase() {
        Mockito.doNothing().when(applicationReceivedNotification).sendToSubject(Mockito.any(CaseData.class), Mockito.any());
        Mockito.doNothing().when(caseWithdrawnNotification).sendToSubject(Mockito.any(CaseData.class), Mockito.any());
        Page page = getPage();
        Login login = new Login(page);
        login.signInWithCaseworker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.startNextStepAction("Case: Close case");
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
        Assertions.assertEquals("Case closed", newCase.getCaseStatus());
    }
}
