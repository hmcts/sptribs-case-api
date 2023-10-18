package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import uk.gov.hmcts.sptribs.e2e.enums.Actions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.CicaContactParties;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.ContactParties;
import static uk.gov.hmcts.sptribs.e2e.enums.Actions.UploadDocuments;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.Representative;
import static uk.gov.hmcts.sptribs.e2e.enums.CaseState.CaseManagement;
import static uk.gov.hmcts.sptribs.testutils.AssertionHelpers.textOptionsWithTimeout;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.clickLink;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCaseUrl;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;

public class ContactPartiesTests extends Base {

    @Order(1)
    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToContactParties() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase(Representative);
        newCase.buildCase();
        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments();
        newCase.startNextStepAction(ContactParties);
        completeContactPartiesJourney(page, ContactParties);
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    @Order(2)
    @Disabled
    public void respondentShouldBeAbleToContactParties() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        final String caseNumber = newCase.createCase(Representative);
        newCase.buildCase();
        newCase.startNextStepAction(UploadDocuments);
        DocumentManagement docManagement = new DocumentManagement(page);
        docManagement.uploadDocuments();
        clickLink(page, "Sign out");
        login.loginAsStRespondentUser();
        page.navigate(getCaseUrl(caseNumber));
        assertThat(page.locator("ccd-markdown markdown h3").first())
            .hasText("Case number: " + caseNumber, textOptionsWithTimeout(60000));
        newCase.startNextStepAction(CicaContactParties);
        completeContactPartiesJourney(page, CicaContactParties);
        Assertions.assertEquals(CaseManagement.label, newCase.getCaseStatus());
    }

    private void completeContactPartiesJourney(Page page, Actions parties) {
        assertThat(page.locator("h1")).hasText("Documents to include", textOptionsWithTimeout(60000));
        getCheckBoxByLabel(page, "sample_file.pdf").check();
        clickButton(page, "Continue");
        if(parties.equals(CicaContactParties)) {
            assertThat(page.locator("h1")).hasText("Which parties do you want to contact?", textOptionsWithTimeout(60000));
        }
        else {
            assertThat(page.locator("h1")).hasText("Contact Parties", textOptionsWithTimeout(60000));
        }
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

