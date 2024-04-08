package uk.gov.hmcts.sptribs.cftlib;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.mockito.Mockito;
import uk.gov.hmcts.sptribs.cftlib.action.Case;
import uk.gov.hmcts.sptribs.cftlib.util.Login;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

public class CreateCaseTest extends XuiTest {

    @RepeatedIfExceptionsTest
    public void createCase() {
        Mockito.doNothing().when(applicationReceivedNotification).sendToSubject(Mockito.any(CaseData.class), Mockito.any());
        Page page = getPage();
        Login login = new Login(page);
        login.signInWithCaseworker();
        Case newCase = new Case(page);
        newCase.createCase();
    }

}
