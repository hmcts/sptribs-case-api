package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import uk.gov.hmcts.sptribs.e2e.enums.CaseParties;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.EditCicaCaseDetails;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getValueFromTableFor;

public class EditCicaCaseDetailsTests extends Base {

    @Disabled
    public void respondentShouldBeAbleToAddEditCicDetails() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        final String caseNumber = newCase.createCase(CaseParties.Representative.label);
        newCase.buildCase();
        clickLink(page, "Sign out");
        login.loginAsStRespondentUser();
        page.navigate(getCaseUrl(caseNumber));
        assertThat(page.locator("ccd-markdown markdown h3").first())
            .hasText("Case number: " + caseNumber, textOptionsWithTimeout(60000));

        newCase.startNextStepAction(EditCicaCaseDetails);
        assertThat(page.locator("h1")).hasText("Case details", textOptionsWithTimeout(60000));
        String cicaReferenceNumber = "Test123";
        page.getByLabel("CICA reference number").type(cicaReferenceNumber);
        String cicaCaseWorker = "Test user";
        page.getByLabel("CICA case worker").type(cicaCaseWorker);
        String cicaCasePresentingOfficer = "Test Officer";
        page.getByLabel("CICA case presenting officer").type(cicaCasePresentingOfficer);
        page.locator("//h1[contains(text(), 'Case details')]").click();
        clickButton(page, "Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case details updated.", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("ccd-markdown markdown h3").first())
            .hasText("Case number: " + caseNumber, textOptionsWithTimeout(60000));

        getTabByText(page, "CICA Details").click();
        getTabByText(page, "CICA Details").click();
        assertThat(page.locator("h4").first()).hasText("CICA Details");
        String referenceNumber = getValueFromTableFor(page, "CICA reference number");
        String caseWorker = getValueFromTableFor(page, "CICA case worker");
        String presentingOfficer = getValueFromTableFor(page, "CICA case presenting officer");
        Assertions.assertEquals(cicaReferenceNumber, referenceNumber);
        Assertions.assertEquals(cicaCaseWorker, caseWorker);
        Assertions.assertEquals(cicaCasePresentingOfficer, presentingOfficer);
    }
}
