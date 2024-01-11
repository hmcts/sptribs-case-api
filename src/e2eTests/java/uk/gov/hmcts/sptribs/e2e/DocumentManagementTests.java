package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.AmendDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.RemoveDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseNumber;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class DocumentManagementTests extends Base {

    private final String documentCategory = "B - Police evidence";
    private final String documentDescription = "This is a test to upload document";
    private final String documentName = "sample_B_police_evidence_document.pdf";

    @Order(1)
    @Disabled
    public void caseWorkerShouldBeAbleToUploadDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);
        newCase.startNextStepAction(UploadDocuments);

        assertThat(page.locator("h1")).hasText("Upload case documents", textOptionsWithTimeout(60000));
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments(documentCategory, documentDescription, documentName);

        getTabByText(page, "Case Documents").click();
        assertThat(page.locator("h4").first()).hasText("Case Documents");
        Assertions.assertEquals(documentCategory, getValueFromTableWithinCaseDocumentsTabFor(page, "Document Category"));
        Assertions.assertEquals(documentDescription, getValueFromTableWithinCaseDocumentsTabFor(page, "Description"));
        Assertions.assertEquals(documentName, getValueFromTableWithinCaseDocumentsTabFor(page, "File"));
    }

    @Order(2)
    @Disabled
    public void seniorCaseWorkerShouldBeAbleToAmendDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);

        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments(documentCategory, documentDescription, documentName);

        newCase.startNextStepAction(AmendDocuments);
        String amendedDocumentCategory = "E - Care plan";
        String amendedDocumentDescription = "This is a test to amend document";
        String amendedDocumentName = "sample_E_care_plan_records_document.pdf";
        docManagement.amendDocument(amendedDocumentCategory, amendedDocumentDescription, amendedDocumentName);

        getTabByText(page, "Case Documents").click();
        assertThat(page.locator("h4").first()).hasText("Case Documents");
        Assertions.assertEquals(amendedDocumentCategory, getValueFromTableWithinCaseDocumentsTabFor(page, "Document Category"));
        Assertions.assertEquals(amendedDocumentDescription, getValueFromTableWithinCaseDocumentsTabFor(page, "Description"));
        Assertions.assertEquals(amendedDocumentName, getValueFromTableWithinCaseDocumentsTabFor(page, "File"));
    }

    @Order(3)
    @Disabled
    public void seniorJudgeShouldBeAbleToRemoveDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);
        final String caseNumber = getCaseNumber(page);

        newCase.startNextStepAction(UploadDocuments);
        String documentToBeRemovedCategory = "L - Linked docs";
        String documentToBeRemovedDescription = "This is a test to remove document";
        String nameOfDocumentToBeRemoved = "sample_L_linked_docs_records_document.docx";
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments(documentToBeRemovedCategory, documentToBeRemovedDescription, nameOfDocumentToBeRemoved);

        clickLink(page, "Sign out");
        Login login = new Login(page);
        login.loginAsSeniorJudge();
        page.navigate(getCaseUrl(caseNumber));
        assertThat(page.locator("ccd-markdown markdown h3").first())
            .hasText("Case number: " + caseNumber, textOptionsWithTimeout(60000));

        newCase.startNextStepAction(RemoveDocuments);
        docManagement.removeDocument(documentToBeRemovedCategory, documentToBeRemovedDescription, nameOfDocumentToBeRemoved);

        getTabByText(page, "Case Documents").click();
        assertThat(page.locator("h4").first()).hasText("Case Documents");
        assertThat(page.locator("//span[contains(text(), '" + documentToBeRemovedCategory + "')]")).isHidden();
    }

    private Case createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsCaseWorker();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        return newCase;
    }

    private String getValueFromTableWithinCaseDocumentsTabFor(Page page, String rowHeader) {
        return page.locator("table.collection-field-table th:has-text(\"" + rowHeader + "\") + td").last().textContent().trim();
    }
}
