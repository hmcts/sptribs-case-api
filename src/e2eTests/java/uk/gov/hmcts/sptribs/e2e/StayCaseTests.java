package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.RemoveStay;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseStayed;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;

public class StayCaseTests extends Base {


    @Order(1)
    @Disabled
    public void caseWorkerShouldBeAbleToAddStayToCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.createCase("applicant", "representative");
        newCase.buildCase();
        newCase.addStayToCase();
    }

    @Order(2)
    @Disabled
    public void caseWorkerShouldBeAbleEditStayToCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.addStayToCase();
        page.selectOption("#next-step", new SelectOption().setLabel("Stays: Create/edit stay"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(6000));
        PageHelpers.clickButton(page, "Go");
        assertThat(page.locator("h1")).hasText("Add a Stay to this case",textOptionsWithTimeout(60000));
        page.getByLabel("Awaiting a court judgement").check();
        page.getByLabel("Year").click();
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Expiration Date")).locator("div").nth(3).click();
        page.getByLabel("Year").fill("2021");
        page.getByLabel("Provide additional details (Optional)").click();
        page.getByLabel("Provide additional details (Optional)").fill("Additional info for create stay case run time");
        clickButton(page, "Continue");
        clickButton(page, "Save and continue");
        assertThat(page.locator("h1:has-text('Stay Added to Case')")).isVisible();
        assertThat(page.locator("h2")).hasText("A notification has been sent to: Subject",textOptionsWithTimeout(30000));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("State")).getByText("State").click();
        page.waitForSelector("h4", PageHelpers.selectorOptionsWithTimeout(60000));
        assertThat(page.locator("h4")).containsText(CaseStayed.label);
    }

    @Order(3)
    @Disabled
    public void caseworkerShouldBeAbleToRemoveStayCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.addStayToCase();
        newCase.startNextStepAction(RemoveStay);
        assertThat(page.locator("h1")).hasText("Remove stay from this case",textOptionsWithTimeout(60000));
        page.getByLabel("Received outcome of criminal proceedings").check();
        clickButton(page, "Continue");
        clickButton(page, "Save and continue");
        assertThat(page.locator("h1"))
            .hasText("Stays: Remove stay",textOptionsWithTimeout(60000));
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Stay Removed from Case", textOptionsWithTimeout(60000));
        assertThat(page.locator("h1:has-text('Stay Removed from Case')")).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
    }
}
