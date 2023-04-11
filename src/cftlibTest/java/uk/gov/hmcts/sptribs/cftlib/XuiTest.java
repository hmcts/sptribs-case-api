package uk.gov.hmcts.sptribs.cftlib;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;
import uk.gov.hmcts.sptribs.cftlib.util.Wiremock;
import uk.gov.hmcts.sptribs.common.notification.ApplicationReceivedNotification;
import uk.gov.hmcts.sptribs.common.notification.CaseWithdrawnNotification;

import static java.lang.System.getenv;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class XuiTest extends CftlibTest {

    Playwright playwright;
    Browser browser;

    BrowserContext context;
    Page page;

    @MockBean
    public ApplicationReceivedNotification applicationReceivedNotification;
    @MockBean
    public CaseWithdrawnNotification caseWithdrawnNotification;
    static final String BASE_URL = "http://localhost:3000";

    protected Page getPage() {
        return page;
    }

    @BeforeAll
    void launchBrowser() {
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
        Wiremock.stopAndReset();
    }


    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(30000);
        page.setDefaultNavigationTimeout(30000);
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

}

