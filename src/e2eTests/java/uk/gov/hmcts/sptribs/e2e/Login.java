package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.Base.BASE_URL;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.functionOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.loadStateOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.selectorOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.urlOptionsWithTimeout;

public class Login {
    private final Page page;

    public Login(Page page) {
        this.page = page;
    }

    private void loginAs(String user) {
        page.waitForSelector("h1:has-text(\"Sign in\")", selectorOptionsWithTimeout(120000));
        if (page.isVisible("#cookie-accept-submit")) {
            clickButton(page, "Accept additional cookies");
            page.locator("button[name=\"hide-accepted\"]").click();
        }
        enterCredentialsAndClickSignIn(user);
        page.waitForURL(BASE_URL + "/cases", urlOptionsWithTimeout(120000));
        assertThat(page.locator("h1"))
            .hasText("Case list", textOptionsWithTimeout(90000));
        if (page.isVisible("button[value=\"accept\"][name=\"cookies\"]")) {
            clickButton(page, "Accept analytics cookies");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED,loadStateOptionsWithTimeout(120000));
            page.waitForURL(BASE_URL + "/cases", urlOptionsWithTimeout(90000));
            page.waitForSelector("h1", selectorOptionsWithTimeout(90000));
            assertThat(page.locator("h1"))
                .hasText("Case list", textOptionsWithTimeout(90000));
        }
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(120000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 0",
            "#wb-jurisdiction", functionOptionsWithTimeout(120000));
    }

    private void enterCredentialsAndClickSignIn(String user) {
        int i = 0;
        while (page.locator("h1:has-text(\"Sign in\")").count() > 0 && i < 5) {
            getTextBoxByLabel(page, "Email address").fill(user);
            getTextBoxByLabel(page, "Password").fill("Pa55w0rd11");
            page.locator("input:has-text(\"Sign in\")").click(clickOptionsWithTimeout(120000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, loadStateOptionsWithTimeout(120000));
            page.waitForSelector("h1", selectorOptionsWithTimeout(120000));
            i++;
        }
    }

    public void loginAsStTest1User() {
        loginAs("st-test1@mailinator.com");
    }

    public void loginAsStJudge2User() {
        loginAs("st-judge2@mailinator.com");
    }

    public void loginAsStSuperUser() {
        loginAs("st-super@mailinator.com");
    }
}
