package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;

import java.util.Objects;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CreateABundle;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.containsTextOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getInnerTextContentFromTableFor;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getValueFromTableFor;

public class BundlingTests extends Base {

    @Order(1)
    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToCreateBundle() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);

        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);

        String documentCategory = "C - GP records";
        String documentDescription = "This is a test to create bundled document";
        String documentName = "sample_C_GP_records_document.pdf";
        docManagement.uploadDocuments(documentCategory, documentDescription, documentName);

        newCase.startNextStepAction(CreateABundle);
        assertThat(page.locator("h1")).hasText("Bundle: Create a bundle", textOptionsWithTimeout(60000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("div.hmcts-banner__message div.alert-message"))
            .containsText("has been updated with event: Bundle: Create a bundle", containsTextOptionsWithTimeout(60000));
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
        getTabByText(page, "Bundles").click();
        getTabByText(page, "Bundles").click();
        assertThat(page.locator("div th#case-viewer-field-label div.case-viewer-label")).hasText("Bundles");
        String configType = getValueFromTableFor(page, "Config used for bundle");
        Assertions.assertEquals("CIC Bundle", configType);
        Assertions.assertTrue(isBundleCreated(page));
    }

    private Case createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsCaseWorker();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        return newCase;
    }

    private boolean isBundleCreated(Page page) {
        boolean bundleCreated = false;
        long timeout = 60000;
        int pollingInterval = 1000;
        long startTime = System.currentTimeMillis();
        Login login = new Login(page);

        while (System.currentTimeMillis() - startTime  < timeout) {
            login.refreshPageWhenServerErrorIsDisplayed();
            boolean stitchedDocumentVisible = page.locator("//table//th/span[contains(text(), 'Stitched Document')]").isVisible();
            if (stitchedDocumentVisible) {
                String innerText = getInnerTextContentFromTableFor(page, "Stitched Document");
                bundleCreated = Objects.equals(innerText, "-cicBundle.pdf");
                break;
            }
            page.reload();
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return bundleCreated;
    }
}
