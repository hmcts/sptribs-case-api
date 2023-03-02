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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static java.lang.System.getenv;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class Base {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    protected Page getPage() {
        return page;
    }

    private BrowserContext context;

    protected static String BASE_URL;
    protected static final String AAT_URL = "https://manage-case.aat.platform.hmcts.net";

    @BeforeAll
    public void setUp() {
        playwright = Playwright.create();

        var launchOptions = getenv("CI") == null
            ? new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(50)
            : new BrowserType.LaunchOptions().setHeadless(false);

        var browserType = getenv("BROWSER") == null ? "chromium" : getenv("BROWSER").toLowerCase();

        switch (browserType) {
            case "msedge", "chrome" -> browser = playwright.chromium().launch(launchOptions.setChannel(browserType));
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
        if (getenv("BROWSER").toLowerCase() == "firefox") {
            Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions();
            newContextOptions.ignoreHTTPSErrors = true;
            context = browser.newContext(newContextOptions);
        } else {
            context = browser.newContext();
        }
        page = context.newPage();
        page.setDefaultTimeout(30000);
        page.setDefaultNavigationTimeout(30000);
        BASE_URL = getenv("BASE_URL") == null ? AAT_URL : getenv("BASE_URL");
        page.navigate(BASE_URL, new Page.NavigateOptions().setTimeout(90000));
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }
}
