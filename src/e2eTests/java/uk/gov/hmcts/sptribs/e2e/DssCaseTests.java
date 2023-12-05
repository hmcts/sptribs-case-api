package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.DssSubmitted;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.visibleOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;

public class DssCaseTests extends Base {

    @Order(1)
    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToSeeTheDssCase() {
        Page page = getPage();
        DssCase newDssCase = new DssCase(page);
        newDssCase.navigateToDssLoginPage();
        Login login = new Login(page);
        login.loginAsStCitizen1User();
        final String caseNumber = newDssCase.createCase("representative");
        clickLink(page, "Sign out");
        login.refreshPageWhenServerErrorIsDisplayed();
        assertH1Heading(page, "Submit a First-tier Tribunal form");
        page.navigate(CASE_API_BASE_URL, new Page.NavigateOptions().setTimeout(90000));
        login.loginAsStTest1User();
        page.navigate(getCaseUrl(caseNumber));
        Case dssCase = new Case(page);
        assertThat(page.locator(".mat-tab-list")).isVisible(visibleOptionsWithTimeout(60000));
        Assertions.assertEquals(DssSubmitted.label, dssCase.getCaseStatus());
    }

    @Order(2)
    @RepeatedIfExceptionsTest
    public void citizenShouldBeAbleToCreateCaseAfterAnsweringPcqQuestionnaire() {
        Page page = getPage();
        DssCase newDssCase = new DssCase(page);
        newDssCase.navigateToDssLoginPage();
        Login login = new Login(page);
        login.loginAsStCitizen1User();
        final String caseNumber = newDssCase.createCase("representative", "pcq");
        clickLink(page, "Sign out");
        login.refreshPageWhenServerErrorIsDisplayed();
        assertH1Heading(page, "Submit a First-tier Tribunal form");
        page.navigate(CASE_API_BASE_URL, new Page.NavigateOptions().setTimeout(90000));
        login.loginAsStTest1User();
        page.navigate(getCaseUrl(caseNumber));
        Case dssCase = new Case(page);
        assertThat(page.locator(".mat-tab-list")).isVisible(visibleOptionsWithTimeout(60000));
        Assertions.assertEquals(DssSubmitted.label, dssCase.getCaseStatus());
    }

    private void assertH1Heading(Page page, String heading) {
        assertThat(page.locator("h1").first())
            .hasText(heading, textOptionsWithTimeout(60000));
    }
}
