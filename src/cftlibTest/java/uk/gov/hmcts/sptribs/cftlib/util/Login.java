package uk.gov.hmcts.sptribs.cftlib.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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
    }

    public void signInWithCaseworker() {
        signInWith("TEST_CASE_WORKER_USER@mailinator.com");
    }

    public void signInWithSolicitor() {
        signInWith("TEST_SOLICITOR@mailinator.com");
    }
}
