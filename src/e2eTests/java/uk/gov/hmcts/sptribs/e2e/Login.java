package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.System.getenv;
import static uk.gov.hmcts.sptribs.e2e.Base.CASE_API_BASE_URL;
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

    private static final String CASE_API_USER_PASSWORD = "Pa55w0rd11";

    public Login(Page page) {
        this.page = page;
    }

    private void loginAs(String user) {
        refreshPageWhenServerErrorIsDisplayed();
        page.waitForSelector("h1:has-text(\"Sign in\")", selectorOptionsWithTimeout(120000));
        if (page.isVisible("#cookie-accept-submit")) {
            clickButton(page, "Accept additional cookies");
            page.locator("button[name=\"hide-accepted\"]").click();
        }
        refreshPageWhenServerErrorIsDisplayed();
        enterCredentialsAndClickSignIn(user);
        refreshPageWhenServerErrorIsDisplayed();
        page.waitForURL(CASE_API_BASE_URL + "/cases", urlOptionsWithTimeout(120000));
        assertThat(page.locator("h1"))
            .hasText("Case list", textOptionsWithTimeout(90000));
        if (page.isVisible("button[value=\"accept\"][name=\"cookies\"]")) {
            clickButton(page, "Accept analytics cookies");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED,loadStateOptionsWithTimeout(120000));
            refreshPageWhenServerErrorIsDisplayed();
            page.waitForURL(CASE_API_BASE_URL + "/cases", urlOptionsWithTimeout(90000));
            page.waitForSelector("h1", selectorOptionsWithTimeout(90000));
            assertThat(page.locator("h1"))
                .hasText("Case list", textOptionsWithTimeout(90000));
        }
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(120000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 0",
            "#wb-jurisdiction", functionOptionsWithTimeout(120000));
        page.waitForSelector("xuilib-loading-spinner div.spinner-inner-container",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED).setTimeout(60000));
    }

    private void enterCredentialsAndClickSignIn(String user) {
        int i = 0;
        while (page.locator("h1:has-text(\"Sign in\")").count() > 0 && i < 5) {
            getTextBoxByLabel(page, "Email address").fill(user);
            getTextBoxByLabel(page, "Password").fill(CASE_API_USER_PASSWORD);
            page.locator("input:has-text(\"Sign in\")").click(clickOptionsWithTimeout(120000));
            refreshPageWhenServerErrorIsDisplayed();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, loadStateOptionsWithTimeout(120000));
            page.waitForSelector("h1", selectorOptionsWithTimeout(120000));
            i++;
        }
    }

    public void loginAsCaseWorker() {
        loginAs(getenv("LEGAL_OFFICER"));
    }

    public void loginAsSeniorCaseWorker() {
        loginAs(getenv("SENIOR_LEGAL_OFFICER"));
    }

    public void loginAsSeniorJudge() {
        loginAs(getenv("SENIOR_JUDGE"));
    }

    public void loginAsHearingCentreTL() {
        loginAs(getenv("HEARING_CENTRAL_TEAM_LEAD"));
    }

    public void loginAsStJudge2User() {
        loginAs(getenv("JUDGE"));
    }

    public void loginAsStSuperUser() {
        loginAs(getenv("SUPER_USER"));
    }

    public void loginAsStRespondentUser() {
        loginAs(getenv("RESPONDENT"));
    }

    public void refreshPageWhenServerErrorIsDisplayed() {
        int i = 0;
        while (i <= 5) {
            page.waitForSelector("body", selectorOptionsWithTimeout(60000));
            String innerText = page.locator("body").innerText();
            boolean errorDisplayed = innerText.contains("Internal Server Error");
            if (errorDisplayed) {
                page.reload();
            } else {
                break;
            }
            i++;
        }
    }
}
