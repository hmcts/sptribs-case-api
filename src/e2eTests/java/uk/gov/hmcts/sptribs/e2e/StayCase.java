package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;

public class StayCase extends Base {
    @Test
    @Order(1)
    public void caseWorkerShouldBeAbleToAddStayToCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase();
        newCase.buildCase();
        newCase.addStayToCase();
    }

    @Order(2)
    @Test
    public void caseWorkerShouldEditStayToCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase();
        newCase.buildCase();
        newCase.addStayToCase();
        page.selectOption("#next-step", new SelectOption().setLabel("Stays: Create/edit stay"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(6000));
        PageHelpers.clickButton("Go");
        assertThat(page.locator("h1")).hasText("Add a Stay to this case",textOptionsWithTimeout(30000));
        page.getByLabel("Awaiting a court judgement").check();
        page.getByLabel("Year").click();
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Expiration Date")).locator("div").nth(3).click();
        page.getByLabel("Year").fill("2021");
        page.getByLabel("Provide additional details (Optional)").click();
        page.getByLabel("Provide additional details (Optional)").fill("Additional info for create stay case run time");
        clickButton("Continue");
        clickButton("Save and continue");
        assertThat(page.locator("h1:has-text('Stay Added to Case')")).isVisible();
        assertThat(page.locator("h2")).hasText("A notification has been sent to: Subject",textOptionsWithTimeout(30000));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("State")).getByText("State").click();
        page.waitForSelector("h4", PageHelpers.selectorOptionsWithTimeout(60000));
        assertThat(page.locator("h4")).containsText("Case stayed");
    }

    @Order(3)
    @Test
    public void caseworkershouldRemoveStayCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase();
        newCase.buildCase();
        newCase.addStayToCase();
        page.selectOption("#next-step", new SelectOption().setLabel("Stays: Remove stay"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(6000));
        PageHelpers.clickButton("Go");
        assertThat(page.locator("h1")).hasText("Remove stay from this case",textOptionsWithTimeout(30000));
        page.getByLabel("Received outcome of criminal proceedings").check();
        clickButton("Continue");
        clickButton("Save and continue");
        assertThat(page.locator("h1"))
            .hasText("Stays: Remove stay",textOptionsWithTimeout(60000));
        assertThat(page.locator("h1:has-text('Stay Removed from Case')")).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close and Return to case details")).click();
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("State")).click();
        assertThat(page.locator("h4")).containsText("Case management");
    }
}
