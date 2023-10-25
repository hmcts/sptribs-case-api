package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.AmendDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.Representative;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getValueFromTableFor;

public class DocumentManagementTests extends Base {

    private final String documentCategory = "A - Notice of Appeal";
    private final String documentDescription = "This is a test to upload document";
    private final String documentName = "sample_file.pdf";

    @Order(1)
    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToUploadDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page, Representative);

        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments(documentCategory, documentDescription, documentName);

        getTabByText(page, "Case Documents").click();
        assertThat(page.locator("h4").first()).hasText("Case Documents");
        Assertions.assertEquals(documentCategory, getValueFromTableFor(page, "Document Category"));
        Assertions.assertEquals(documentDescription, getValueFromTableFor(page, "Description"));
        Assertions.assertEquals(documentName, getValueFromTableFor(page, "File"));
    }

    @Order(2)
    @RepeatedIfExceptionsTest
    public void seniorCaseWorkerShouldBeAbleToAmendDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page, Representative);

        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments(documentCategory, documentDescription, documentName);

        newCase.startNextStepAction(AmendDocuments);
        String amendedDocumentCategory = "A - Correspondence from the CICA";
        String amendedDocumentDescription = "This is a test to amend document";
        docManagement.amendDocument(amendedDocumentCategory, amendedDocumentDescription, documentName);

        getTabByText(page, "Case Documents").click();
        assertThat(page.locator("h4").first()).hasText("Case Documents");
        Assertions.assertEquals(amendedDocumentCategory, getValueFromTableFor(page, "Document Category"));
        Assertions.assertEquals(amendedDocumentDescription, getValueFromTableFor(page, "Description"));
        Assertions.assertEquals(documentName, getValueFromTableFor(page, "File"));
    }
}
