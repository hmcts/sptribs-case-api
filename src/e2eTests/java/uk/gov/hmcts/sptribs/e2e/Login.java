package uk.gov.hmcts.sptribs.e2e;

import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Login extends Base {
    private void loginAs(String user) {
        page.waitForSelector("h1", PageHelpers.selectorOptionsWithTimeout(60000));
        String heading = page.textContent("h1");
        assertEquals("Sign in or create an account", heading);
        PageHelpers.clickButton("Accept additional cookies");
        page.locator("button[name=\"hide-accepted\"]").click();
        PageHelpers.getTextBoxByLabel("Email address").fill(user);
        PageHelpers.getTextBoxByLabel("Password").fill("Pa55w0rd11");
        PageHelpers.clickButton("Sign in");
        page.waitForURL(BASE_URL + "/cases", PageHelpers.urlOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case list'",
            "h1", PageHelpers.functionOptionsWithTimeout(60000));
        if (page.isVisible("button[value=\"accept\"][name=\"cookies\"]")) {
            PageHelpers.clickButton("Accept analytics cookies");
            page.waitForURL(BASE_URL + "/cases", PageHelpers.urlOptionsWithTimeout(60000));
            page.waitForSelector("h1", PageHelpers.selectorOptionsWithTimeout(60000));
            page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case list'",
                "h1", PageHelpers.functionOptionsWithTimeout(60000));
        }
        page.waitForSelector("h2.heading-h2", PageHelpers.selectorOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 0",
            "#wb-jurisdiction", PageHelpers.functionOptionsWithTimeout(60000));
    }

    public void loginAsStTest1User() {
        loginAs("st-test1@mailinator.com");
    }

    public void loginAsStSuperUser() {
        loginAs("st-super@mailinator.com");
    }
}
