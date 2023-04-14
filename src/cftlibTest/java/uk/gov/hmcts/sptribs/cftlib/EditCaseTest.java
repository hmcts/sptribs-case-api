package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.sptribs.cftlib.action.Case;
import uk.gov.hmcts.sptribs.cftlib.util.Login;

public class EditCaseTest extends XuiTest {

    @Test
    public void caseWorkerShouldBeAbleToEditCase() {
        Mockito.doNothing().when(applicationReceivedNotification).sendToSubject(Mockito.any(), Mockito.any());
        Page page = getPage();
        Login login = new Login(page);
        login.signInWithCaseworker();
        Case newCase = new Case(page);
        newCase.createCase();

        newCase.editCase();

    }
}
