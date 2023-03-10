package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;

public class CreateFlag extends Base {

    @Test
    public void caseWorkerShouldBeAbleToCreateACaseFlag() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.createCaseFlag();
    }

    @Test
    public void caseWorkerShouldAbleToDoManageFlags() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.createCaseFlag();
        newCase.startNextStepAction("Flags: Manage flags");
        assertThat(page.locator("h1")).hasText("Flags: Manage flags", textOptionsWithTimeout(60000));
        page.getByLabel("Flag Type (Optional)").click();
        page.getByLabel("Flag Type (Optional)").fill("test flag");
        page.getByLabel("Party Name (Optional)").click();
        page.getByLabel("Party Name (Optional)").fill("case party");
        page.getByLabel("Value (Optional)").click();
        page.getByLabel("Value (Optional)").fill("test 6");
        page.getByLabel("Key (Optional)").click();
        page.getByLabel("Key (Optional)").fill("test123");
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Day").click();
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Day").fill("8");
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Month").fill("6");
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Year").fill("2021");
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Hour").fill("7");
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Minute").fill("5");
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Modified Date (Optional)")).getByLabel("Second").fill("22");
        page.getByLabel("No").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Flags: Manage flags",textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }
}









