package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.RetryingTest;
import uk.gov.hmcts.sptribs.cftlib.action.Case;
import uk.gov.hmcts.sptribs.cftlib.action.Hearing;
import uk.gov.hmcts.sptribs.cftlib.util.Login;
import uk.gov.hmcts.sptribs.cftlib.util.PlaywrightHelpers;

public class CaseworkerRecordListingTest extends XuiTest {

    @RetryingTest(maxAttempts = PlaywrightHelpers.RETRIES)
    public void caseworkerShouldAbleToCloseTheCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.signInWithCaseworker();

        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();

        Hearing hearing = new Hearing(page);
        hearing.createListing();
        Assertions.assertEquals("Awaiting hearing", newCase.getCaseStatus());

    }
}
