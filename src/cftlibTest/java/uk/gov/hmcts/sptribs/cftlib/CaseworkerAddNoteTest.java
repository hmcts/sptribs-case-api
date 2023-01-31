package uk.gov.hmcts.sptribs.cftlib;

import org.junitpioneer.jupiter.RetryingTest;
import uk.gov.hmcts.sptribs.cftlib.util.PlaywrightHelpers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CaseworkerAddNoteTest extends XuiTest {

    @RetryingTest(maxAttempts = PlaywrightHelpers.RETRIES)
    public void addNote() {
        signInWithCaseworker();
        page.locator("text=Create case").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-filter");
        page.locator("select[name=\"case-type\"]").selectOption("CIC");
        page.locator("text=Start").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/ST_CIC/create-test-application/create-test-applicationcaseCategorisationDetails");
    }
}
