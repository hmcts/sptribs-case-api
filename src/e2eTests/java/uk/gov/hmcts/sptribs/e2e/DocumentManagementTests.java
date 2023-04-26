package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Disabled;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.functionOptionsWithTimeout;

public class DocumentManagementTests extends Base {

    @Disabled
    public void caseWorkerShouldBeAbleToUploadDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);
        newCase.startNextStepAction(UploadDocuments);

        assertThat(page.locator("h1")).hasText("Upload case documents", textOptionsWithTimeout(60000));
        uploadDocuments(page);
        clickButton(page, "Continue");

        assertThat(page.locator(".check-your-answers h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");

        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
    }

    private Case createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        return newCase;
    }

    private void uploadDocuments(Page page) {
        clickButton(page, "Add new");
        page.selectOption("#caseworkerCICDocument_0_documentCategory", new SelectOption().setLabel("Tribunal Direction"));
        page.locator("#caseworkerCICDocument_0_documentEmailContent").fill("This is a test document");
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/sample_file.pdf"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === true",
            "button[aria-label='Cancel upload']", functionOptionsWithTimeout(15000));
    }
}
