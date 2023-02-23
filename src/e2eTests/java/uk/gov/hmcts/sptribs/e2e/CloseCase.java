package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import java.util.Calendar;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;


public class CloseCase extends Base {
    @Test
    @Order(1)
    public void caseworkerShouldAbleToClosetheCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase();
        newCase.buildCase();
        page.selectOption("#next-step", new SelectOption().setLabel("Case: Close case"));
        clickButton("Go");
        assertThat(page.locator("h1")).hasText("Are you sure you want to close this case?",textOptionsWithTimeout(30000));
        clickButton("Continue");
        page.getByLabel("Case Withdrawn").check();
        clickButton("Continue");
        assertThat(page.locator("h1")).hasText("Withdrawal details",textOptionsWithTimeout(30000));
        page.getByLabel("Who withdrew from the case?").fill("case worker");
        Calendar date = DateHelpers.getYesterdaysDate();
        getTextBoxByLabel("Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        getTextBoxByLabel("Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        getTextBoxByLabel("Year").type(String.valueOf(date.get(Calendar.YEAR)));
        page.locator("h1").click();
        clickButton("Continue");
        assertThat(page.locator("h1")).hasText("Upload case documents",textOptionsWithTimeout(30000));
        clickButton("Continue");
        assertThat(page.locator("h1")).hasText("Select recipients",textOptionsWithTimeout(30000));
        page.getByLabel("Subject").check();
        page.getByLabel("Respondent").check();
        clickButton("Continue");
        assertThat(page.locator("h2")).hasText("Check your answers",textOptionsWithTimeout(30000));
        clickButton("Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case closed", textOptionsWithTimeout(60000));
        clickButton("Close and Return to case details");
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("State")).getByText("State").click();
        page.waitForSelector("h4", PageHelpers.selectorOptionsWithTimeout(60000));
        assertThat(page.locator("h4")).containsText("Case Status:  Case closed");
    }

    @Test
    public void caseworkerShouldAbleToReinstantCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase();
        newCase.buildCase();
        newCase.caseworkerShouldAbleToClosetheCase();
        page.selectOption("#next-step", new SelectOption().setLabel("Case: Reinstate case"));
        clickButton("Go");
        assertThat(page.locator("h1")).hasText("Are you sure you want to reinstate this case?", textOptionsWithTimeout(30000));
        clickButton("Continue");
        assertThat(page.locator("h1")).hasText("Reason for reinstatement", textOptionsWithTimeout(30000));
        page.getByLabel("Request following a withdrawal decision").check();
        clickButton("Continue");
        assertThat(page.locator("h1")).hasText("Upload documents", textOptionsWithTimeout(30000));
        clickButton("Continue");
        assertThat(page.locator("h1")).hasText("Contact parties", textOptionsWithTimeout(30000));
        clickButton("Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton("Save and continue");
        assertThat(page.locator("h1")).hasText("Case: Reinstate case", textOptionsWithTimeout(30000));
        clickButton("Close and Return to case details");
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("State")).getByText("State").click();
    }
}



