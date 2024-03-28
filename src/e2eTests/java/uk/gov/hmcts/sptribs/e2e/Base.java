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

    protected static final String CASE_API_AAT_URL = "https://manage-case.aat.platform.hmcts.net";

    public static String CASE_API_BASE_URL;

    @BeforeAll
    public void setUp() {
        playwright = Playwright.create();

        final BrowserType.LaunchOptions launchOptions = getenv("CI").equalsIgnoreCase("true")
            ? new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(100)
            : new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100);

        final String browserType = getenv("BROWSER") == null ? "chromium" : getenv("BROWSER").toLowerCase();

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
        if (getenv("BROWSER").equalsIgnoreCase("firefox")) {
            Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions();
            newContextOptions.ignoreHTTPSErrors = true;
            context = browser.newContext(newContextOptions);
        } else {
            context = browser.newContext();
        }
        page = context.newPage();
        page.setDefaultTimeout(30000);
        page.setDefaultNavigationTimeout(60000);
        String url = getenv("CASE_API_BASE_URL");
        CASE_API_BASE_URL = url == null || url.equals("null") ? CASE_API_AAT_URL : url;
        page.navigate(CASE_API_BASE_URL, new Page.NavigateOptions().setTimeout(120000));
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }
}
