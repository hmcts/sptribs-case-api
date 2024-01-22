package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.AddNote;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseParties.Representative;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTextBoxByLabel;

public class AddNoteTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToAddNote() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase(Representative.label);
        newCase.buildCase();
        newCase.startNextStepAction(AddNote);
        assertThat(page.locator("h1")).hasText("Add case notes", textOptionsWithTimeout(60000));
        String noteText = "This is a test to add note";
        getTextBoxByLabel(page, "Add a case note").type(noteText);
        page.locator("div h1").click();
        clickButton(page, "Submit");

        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());

        getTabByText(page, "Notes").click();
        Assertions.assertEquals(noteText, page.locator("ccd-read-text-area-field span").textContent());
    }
}
