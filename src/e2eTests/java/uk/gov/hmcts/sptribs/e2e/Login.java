package uk.gov.hmcts.sptribs.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.functionOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.selectorOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.urlOptionsWithTimeout;

public class Login extends Base {
    private void loginAs(String user) {
        page.waitForSelector("h1", selectorOptionsWithTimeout(60000));
        String heading = page.textContent("h1");
        assertEquals("Sign in or create an account", heading);
        clickButton("Accept additional cookies");
        page.locator("button[name=\"hide-accepted\"]").click();
        getTextBoxByLabel("Email address").fill(user);
        getTextBoxByLabel("Password").fill("Pa55w0rd11");
        clickButton("Sign in");
        page.waitForURL(BASE_URL + "/cases", urlOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case list'",
            "h1", functionOptionsWithTimeout(60000));
        if (page.isVisible("button[value=\"accept\"][name=\"cookies\"]")) {
            clickButton("Accept analytics cookies");
            page.waitForURL(BASE_URL + "/cases", urlOptionsWithTimeout(60000));
            page.waitForSelector("h1", selectorOptionsWithTimeout(60000));
            page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case list'",
                "h1", functionOptionsWithTimeout(60000));
        }
        page.waitForSelector("h2.heading-h2", selectorOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 0",
            "#wb-jurisdiction", functionOptionsWithTimeout(60000));
    }

    public void loginAsStTest1User() {
        loginAs("st-test1@mailinator.com");
    }

    public void loginAsStSuperUser() {
        loginAs("st-super@mailinator.com");
    }
}
