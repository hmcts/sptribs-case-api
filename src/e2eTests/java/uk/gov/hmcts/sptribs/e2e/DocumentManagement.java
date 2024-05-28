package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Assertions;

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
        page.selectOption("#cicCaseSelectedDocument_documentCategory", new SelectOption().setLabel(documentCategory));
        page.locator("#cicCaseSelectedDocument_documentEmailContent").type(documentDescription);
        page.locator("//h2[contains(text(), 'Documents')]").click();
        clickButton(page, "Continue");

        assertThat(page.locator(".check-your-answers h2.heading-h2"))
            .hasText("Check your answers", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");

        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Document Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
    }

    public void removeDocument(String documentCategory, String documentDescription, String documentName) {
        assertThat(page.locator("h1")).hasText("Show case documents", textOptionsWithTimeout(60000));
        String documentRemoveSelector = "//div[@class='form-group'][.//div//a[contains(text(), '"
            + documentName + "')]]//button[text()='Remove']";

        page.locator(documentRemoveSelector).click();
        page.locator("ccd-remove-dialog button[title='Remove']").click();
        page.waitForSelector(documentRemoveSelector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        clickButton(page, "Continue");

        assertThat(page.locator("h1")).hasText("Removed documents", textOptionsWithTimeout(60000));
        String displayedDocumentCategory =
            page.locator("//ccd-field-read-label[.//dt[text()='Document Category']]//ccd-read-fixed-list-field/span").textContent().trim();
        String displayedDescription =
            page.locator("//ccd-field-read-label[.//dt[text()='Description']]//ccd-read-text-area-field/span").textContent().trim();
        String displayedFileName =
            page.locator("//ccd-field-read-label[.//dt[text()='File']]//ccd-read-document-field/a").textContent().trim();
        Assertions.assertEquals(documentCategory, displayedDocumentCategory);
        Assertions.assertEquals(documentDescription, displayedDescription);
        Assertions.assertEquals(documentName, displayedFileName);
        clickButton(page, "Submit");

        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case Updated", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");

        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
    }

    private void uploadDocument(Page page, String documentCategory, String documentDescription, String documentName) {
        clickButton(page, "Add new");
        page.selectOption("#newCaseworkerCICDocumentUpload_0_documentCategory", new SelectOption().setLabel(documentCategory));
        page.setInputFiles("input[type='file']", Paths.get("src/e2eTests/java/uk/gov/hmcts/sptribs/testutils/files/" + documentName));
        page.waitForSelector("//span[contains(text(), 'Uploading...')]",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
        page.waitForFunction("selector => document.querySelector(selector).disabled === true",
            "button[aria-label='Cancel upload']", functionOptionsWithTimeout(15000));
        page.locator("#newCaseworkerCICDocumentUpload_0_documentEmailContent").type(documentDescription);
        page.locator("//div[contains(@id, 'newCaseworkerCICDocument')]//h2[contains(text(), 'Documents')]").click();
    }
}
