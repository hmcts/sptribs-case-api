package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ContactParties;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;

public class ContactPartiesTests extends Base {

    @Order(1)
    @Disabled
    public void caseWorkerShouldBeAbleToContactParties() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        newCase.createCase("representative");
        newCase.buildCase();
        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments();
        newCase.startNextStepAction(ContactParties);
        completeContactPartiesJourney(page);
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    @Order(2)
    @Disabled
    public void respondentShouldBeAbleToContactParties() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        final String caseNumber = newCase.createCase("representative");
        newCase.buildCase();
        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments();
        clickLink(page, "Sign out");
        login.loginAsStRespondentUser();
        page.navigate(getCaseUrl(caseNumber));
        newCase.startNextStepAction(ContactParties);
        completeContactPartiesJourney(page);
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    private void completeContactPartiesJourney(Page page) {
        assertThat(page.locator("h1")).hasText("Documents to include", textOptionsWithTimeout(60000));
        getCheckBoxByLabel(page, "sample_file.pdf").check();
        clickButton(page, "Continue");
        assertThat(page.locator("h1")).hasText("Which parties do you want to contact?", textOptionsWithTimeout(60000));
        if (page.isVisible("input[type='checkbox'][value='SubjectCIC']")
            && !page.isChecked("input[type='checkbox'][value='SubjectCIC']")) {
            getCheckBoxByLabel(page, "Subject").check();
        }
        if (page.isVisible("input[type='checkbox'][value='RepresentativeCIC']")
            && !page.isChecked("input[type='checkbox'][value='RepresentativeCIC']")) {
            getCheckBoxByLabel(page, "Representative").check();
        }
        if (page.isVisible("input[type='checkbox'][value='RespondentCIC']")
            && !page.isChecked("input[type='checkbox'][value='RespondentCIC']")) {
            getCheckBoxByLabel(page, "Respondent").check();
        }
        if (page.isVisible("input[type='checkbox'][value='TribunalCIC']")
            && !page.isChecked("input[type='checkbox'][value='TribunalCIC']")) {
            getCheckBoxByLabel(page, "Tribunal").check();
        }
        page.getByLabel("Message").fill("test123");
        clickButton(page, "Continue");
        assertThat(page.locator("h2")).hasText("Check your answers", textOptionsWithTimeout(30000));
        clickButton(page, "Save and continue");
        assertThat(page.locator("ccd-markdown markdown h1"))
            .hasText("Message sent", textOptionsWithTimeout(60000));
        clickButton(page, "Close and Return to case details");
        assertThat(page.locator("h2.heading-h2").first())
            .hasText("History", textOptionsWithTimeout(60000));
    }
}

