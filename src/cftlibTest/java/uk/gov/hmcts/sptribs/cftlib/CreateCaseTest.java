package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;
import uk.gov.hmcts.sptribs.cftlib.action.Case;
import uk.gov.hmcts.sptribs.cftlib.util.Login;

public class CreateCaseTest extends XuiTest {

    @Disabled
    public void createCase() {
        Mockito.doNothing().when(applicationReceivedNotification).sendToSubject(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(applicationReceivedNotification).sendToApplicant(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(applicationReceivedNotification).sendToRepresentative(Mockito.any(), Mockito.any());
        Page page = getPage();
        Login login = new Login(page);
        login.signInWithCaseworker();
        Case newCase = new Case(page);
        newCase.createCase();
    }

}
