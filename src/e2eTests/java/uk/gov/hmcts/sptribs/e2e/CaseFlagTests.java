package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.e2e.enums.CaseParties;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class CaseFlagTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToCreateACaseLevelFlag() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.createCaseLevelFlag();
        assertThat(page.locator("ccd-notification-banner div h2#govuk-notification-banner-title"))
            .hasText("Important", textOptionsWithTimeout(30000));
        assertThat(page.locator("ccd-notification-banner div p.govuk-notification-banner__heading"))
            .hasText("There is 1 active flag on this case. View case flags", textOptionsWithTimeout(30000));
        page.locator("ccd-notification-banner div p a.govuk-notification-banner__link").click();
        Assertions.assertEquals("Active", getCaseLevelFlagStatus(page));
    }

    @RepeatedIfExceptionsTest
    public void caseWorkedShouldBeAbleToCreatePartyLevelFlag() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase(CaseParties.Subject.label, CaseParties.Representative.label, CaseParties.Applicant.label);
        newCase.buildCase();
        newCase.createFlagForParties(CaseParties.Subject, CaseParties.Representative, CaseParties.Applicant);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToManageFlags() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.createCaseLevelFlag();
        newCase.updateCaseLevelFlag();
        getTabByText(page, "Case Flags").click();
        getTabByText(page, "Case Flags").click();
        Assertions.assertEquals("Inactive", getCaseLevelFlagStatus(page));
    }

    private String getCaseLevelFlagStatus(Page page) {
        String selector =
            "//ccd-case-flag-table[.//caption[contains(text(), 'Case level flags')]]//td/strong[contains(@class, 'govuk-tag')]";
        ElementHandle element = page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
        String flagStatusElement = element.textContent();
        return flagStatusElement.trim();
    }
}
