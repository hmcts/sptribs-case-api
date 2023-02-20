package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junitpioneer.jupiter.RetryingTest;
import uk.gov.hmcts.sptribs.cftlib.util.PlaywrightHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CaseworkerAddNoteTest extends XuiTest {

    @RetryingTest(maxAttempts = PlaywrightHelpers.RETRIES)
    public void addNote() {
        signInWithCaseworker();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create case")).click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-filter");
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Jurisdiction")).selectOption("ST_CIC");
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Case type")).selectOption("CriminalInjuriesCompensation");
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Event")).selectOption("caseworker-create-case");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Start")).click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/ST_CIC/CriminalInjuriesCompensation/caseworker-create-case/caseworker-create-casecaseCategorisationDetails");
    }
}
