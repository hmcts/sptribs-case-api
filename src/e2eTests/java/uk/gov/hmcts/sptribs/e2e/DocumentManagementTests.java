package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getTabByText;

public class DocumentManagementTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToUploadDocuments() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);
        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments();
        getTabByText(page, "Case Documents").click();
        assertThat(page.locator("h4").first()).hasText("Case Documents");
        String hearingStatus = PageHelpers.getValueFromTableFor(page, "Document Category");
        Assertions.assertEquals("A - Notice of Appeal", hearingStatus);
        String hearingType = PageHelpers.getValueFromTableFor(page, "Description");
        Assertions.assertEquals("This is a test document", hearingType);
        String hearingFormat = PageHelpers.getValueFromTableFor(page, "File");
        Assertions.assertEquals("sample_file.pdf", hearingFormat);
    }

    private Case createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsCaseWorker();

        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        return newCase;
    }
}
