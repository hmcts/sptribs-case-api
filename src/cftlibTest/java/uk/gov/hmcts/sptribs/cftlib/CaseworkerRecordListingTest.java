package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.RetryingTest;
import uk.gov.hmcts.sptribs.cftlib.action.Actions;
import uk.gov.hmcts.sptribs.cftlib.action.Case;
import uk.gov.hmcts.sptribs.cftlib.util.Login;
import uk.gov.hmcts.sptribs.cftlib.util.PageHelpers;
import uk.gov.hmcts.sptribs.cftlib.util.PlaywrightHelpers;

import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.clickButton;
import static uk.gov.hmcts.sptribs.cftlib.util.PageHelpers.getTextBoxByLabel;

public class CaseworkerRecordListingTest extends XuiTest {

    @RetryingTest(maxAttempts = PlaywrightHelpers.RETRIES)
    public void caseworkerShouldAbleToCloseTheCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.signInWithCaseworker();

        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.startNextStepAction(Actions.CreateListing);

        page.getByLabel("Final").check();
        page.getByLabel("Face to Face").check();
        PageHelpers.clickButton(page, "Continue");
        page.getByLabel("1-London").check();
        PageHelpers.clickButton(page, "Continue");

        page.getByLabel("BARNET CIVIL AND FAMILY COURTS CENTRE-ST MARY'S COURT, REGENTS PARK ROAD").check();
        getTextBoxByLabel(page, "Day").fill("1");
        getTextBoxByLabel(page, "Month").fill("1");
        getTextBoxByLabel(page, "Year").fill("1990");
        page.getByLabel("Morning").check();
        page.getByLabel("Start time (24hr format)").click();
        page.getByLabel("Start time (24hr format)").fill("09:00");
        page.getByLabel("No").check();
        PageHelpers.clickButton(page, "Continue");
        PageHelpers.clickButton(page, "Continue");
        PageHelpers.clickButton(page, "Continue");
        page.getByLabel("Respondent").check();
        PageHelpers.clickButton(page, "Continue");
        clickButton(page, "Save and continue");
        clickButton(page, "Close and Return to case details");
        Assertions.assertEquals("Awaiting hearing", newCase.getCaseStatus());

    }
}
