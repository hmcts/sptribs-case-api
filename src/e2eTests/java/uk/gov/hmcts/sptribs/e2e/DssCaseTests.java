package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.visibleOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;

public class DssCaseTests extends Base {

    @Order(1)
    @Disabled
    public void caseWorkerShouldBeAbleToSeeTheDssCase() {
        Page page = getPage();
        DssCase newCase = new DssCase(page);
        newCase.navigateToDssLoginPage();
        Login login = new Login(page);
        login.loginAsStCitizen1User();
        final String caseNumber = newCase.createCase("representative");
        clickLink(page, "Sign out");
        assertH1Heading(page, "Submit a First-tier Tribunal form");
        page.navigate(CASE_API_BASE_URL, new Page.NavigateOptions().setTimeout(90000));
        login.loginAsStTest1User();
        page.navigate(getCaseUrl(caseNumber));
        assertThat(page.locator(".mat-tab-list")).isVisible(visibleOptionsWithTimeout(60000));
    }

    private void assertH1Heading(Page page, String heading) {
        assertThat(page.locator("h1").first())
            .hasText(heading, textOptionsWithTimeout(60000));
    }
}
