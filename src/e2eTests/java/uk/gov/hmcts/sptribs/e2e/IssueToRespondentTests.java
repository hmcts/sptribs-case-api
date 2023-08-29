package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.IssueCaseToRespondent;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.Representative;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;

public class IssueToRespondentTests extends Base {

    @Disabled
    public void caseWorkerShouldAbleToManageIssueToRespondent() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase(Representative);
        newCase.buildCase();
        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments();
        newCase.startNextStepAction(IssueCaseToRespondent);
        assertThat(page.locator("h1")).hasText("Select additional documents", textOptionsWithTimeout(60000));
        getCheckBoxByLabel(page, "sample_file.pdf").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Notify other parties", textOptionsWithTimeout(60000));
        page.getByLabel("Subject").check();
        page.getByLabel("Representative").check();
        page.getByLabel("Respondent").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h2.heading-h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Case issued", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }
}
