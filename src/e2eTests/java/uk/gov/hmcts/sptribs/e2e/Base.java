package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import static java.lang.System.getenv;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Base {
    private Playwright playwright;
    private Browser browser;
    protected static Page page;
    private BrowserContext context;

    protected static String BASE_URL;
    protected static final String AAT_URL = "https://manage-case.aat.platform.hmcts.net";

    @BeforeAll
    public void setUp() {
        playwright = Playwright.create();
        var launchOptions = getenv("CI") == null
            ? new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50)
            : new BrowserType.LaunchOptions().setHeadless(true);

        var browserType = getenv("BROWSER") == null ? "chromium" : getenv("BROWSER");
        switch (browserType) {
            case "firefox" -> browser = playwright.firefox().launch(launchOptions);
            case "webkit" -> browser = playwright.webkit().launch(launchOptions);
            default -> browser = playwright.chromium().launch(launchOptions);
        }
    }

    @AfterAll
    void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(30000);
        BASE_URL = getenv("BASE_URL") == null ? AAT_URL : getenv("BASE_URL");
        page.navigate(BASE_URL);
    }

    @AfterEach
    void closeContext() {
        context.close();
    }
}
