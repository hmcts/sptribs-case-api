package uk.gov.hmcts.sptribs.cftlib.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.options.LoadState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.cftlib.util.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.loadStateOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.selectorOptionsWithTimeout;

public class Login {
    private Page page;

    public Login(Page page) {
        this.page = page;
    }

    public void signInWith(String username) {
        page.navigate("http://localhost:3000");

        page.locator("[placeholder=\"Enter Username\"]").fill(username);
        page.locator("[placeholder=\"Enter Password\"]").fill("anythingWillWork");
        page.locator("text=Sign in").click();
        assertThat(page).hasURL("http://localhost:3000/cases", new PageAssertions.HasURLOptions().setTimeout(30000));
        if (page.isVisible("button[value=\"accept\"][name=\"cookies\"]")) {
            clickButton(page, "Accept analytics cookies");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED,loadStateOptionsWithTimeout(120000));
            page.waitForSelector("h1", selectorOptionsWithTimeout(90000));
            assertThat(page.locator("h1"))
                .hasText("Case list", textOptionsWithTimeout(90000));
        }
    }

    public void signInWithCaseworker() {
        signInWith("TEST_CASE_WORKER_USER@mailinator.com");
    }

    public void signInWithSolicitor() {
        signInWith("TEST_SOLICITOR@mailinator.com");
    }
}
