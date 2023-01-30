package uk.gov.hmcts.sptribs.e2e;

import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static org.testng.Assert.assertEquals;

public class Login extends Base {
    private void loginAs(String user) {
        page.waitForSelector("h1", PageHelpers.selectorOptionsWithTimeout(60000));
        String heading = page.textContent("h1");
        assertEquals(heading, "Sign in or create an account");
        page.locator("button[value=\"accept\"]").click();
        page.locator("button[name=\"hide-accepted\"]").click();
        page.locator("#username").fill(user);
        page.locator("#password").fill("Pa55w0rd11");
        page.locator("input[type=\"submit\"][name=\"save\"]").click();
        page.waitForURL(BASE_URL + "/cases", PageHelpers.urlOptionsWithTimeout(60000));
        page.waitForFunction("() => document.querySelector('h1').innerText === 'Case list'",
            "h1", PageHelpers.functionOptionsWithTimeout(60000));
        if (page.isVisible("button[value=\"accept\"][name=\"cookies\"]")) {
            page.locator("button[value=\"accept\"][name=\"cookies\"]").click();
            //  page.waitForURL(PAGE_URL + "/cases", PageHelpers.urlOptionsWithTimeout(60000));
            page.waitForSelector("h1", PageHelpers.selectorOptionsWithTimeout(60000));
            page.waitForFunction("() => document.querySelector('h1').innerText === 'Case list'",
                "h1", PageHelpers.functionOptionsWithTimeout(60000));
        }
        page.waitForSelector("h2.heading-h2", PageHelpers.selectorOptionsWithTimeout(60000));
        page.waitForFunction("() => document.querySelector('#wb-jurisdiction').options.length > 0",
            "#wb-jurisdiction", PageHelpers.functionOptionsWithTimeout(60000));
    }

    public void loginAsStTest1User() {
        loginAs("st-test1@mailinator.com");
    }

    public void loginAsStSuperUser() {
        loginAs("st-super@mailinator.com");
    }
}
