package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;

public class LinkCaseTests extends Base {

    @Disabled
    void linkTwoCases() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        String case1 = newCase.createCase();
        newCase.buildCase();
        newCase.createCase();
        newCase.buildCase();
        newCase.linkCase(case1);
    }
}
