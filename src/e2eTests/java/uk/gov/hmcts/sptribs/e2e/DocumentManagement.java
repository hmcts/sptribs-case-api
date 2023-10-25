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

    public void uploadDocuments(String documentCategory, String documentDescription, String documentName) {
        assertThat(page.locator("h1")).hasText("Upload case documents", textOptionsWithTimeout(60000));
        uploadDocument(page, documentCategory, documentDescription, documentName);
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

    public void amendDocument(String documentCategory, String documentDescription, String documentName) {
        assertThat(page.locator("h1")).hasText("Select documents", textOptionsWithTimeout(60000));
        String documentOptionSelector = "//select[@id='cicCaseAmendDocumentList']//option[contains(text(), '" + documentName + "')]";
        String documentToSelect = page.locator(documentOptionSelector).textContent();
        page.selectOption("#cicCaseAmendDocumentList", new SelectOption().setLabel(documentToSelect));
        clickButton(page, "Continue");

        assertThat(page.locator("h1")).hasText("Amend case documents", textOptionsWithTimeout(60000));
        String amendedDocumentCategory = "A - Correspondence from the CICA";
        page.selectOption("#cicCaseSelectedDocument_documentCategory", new SelectOption().setLabel(documentCategory));
        String amendedDocumentDescription = "This is a test to amend document";
        page.locator("#cicCaseSelectedDocument_documentEmailContent").fill(documentDescription);
        clickButton(page, "Continue");

        assertThat(page.locator(".check-your-answers h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");

        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Document Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
    }

    private void uploadDocument(Page page, String documentCategory, String documentDescription, String documentName) {
        clickButton(page, "Add new");
        page.selectOption("#newCaseworkerCICDocument_0_documentCategory", new SelectOption().setLabel(documentCategory));
        page.locator("#newCaseworkerCICDocument_0_documentEmailContent").fill(documentDescription);
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/" + documentName));
        page.waitForFunction("selector => document.querySelector(selector).disabled === true",
            "button[aria-label='Cancel upload']", functionOptionsWithTimeout(15000));
    }
}
