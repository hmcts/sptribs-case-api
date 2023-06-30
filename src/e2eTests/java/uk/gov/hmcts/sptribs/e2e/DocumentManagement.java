package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.functionOptionsWithTimeout;

public class DocumentManagement {
    private final Page page;

    public DocumentManagement(Page page) {
        this.page = page;
    }

    public void uploadDocuments() {
        assertThat(page.locator("h1")).hasText("Upload case documents", textOptionsWithTimeout(60000));
        uploadDocument(page, "sample_file.pdf");
        clickButton(page, "Continue");

        assertThat(page.locator(".check-your-answers h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");

        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
    }

    private void uploadDocument(Page page, String file) {
        clickButton(page, "Add new");
        page.selectOption("#newCaseworkerCICDocument_0_documentCategory", new SelectOption().setLabel("A - Notice of Appeal"));
        page.locator("#newCaseworkerCICDocument_0_documentEmailContent").fill("This is a test document");
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/" + file));
        page.waitForFunction("selector => document.querySelector(selector).disabled === true",
            "button[aria-label='Cancel upload']", functionOptionsWithTimeout(15000));
    }
}
