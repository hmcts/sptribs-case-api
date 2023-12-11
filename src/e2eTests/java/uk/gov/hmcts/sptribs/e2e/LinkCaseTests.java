package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;

public class LinkCaseTests extends Base {

    @RepeatedIfExceptionsTest
    void linkCasesTest() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case case1 = new Case(page);
        String caseId1 = case1.createCase();
        case1.buildCase();

        Case case2 = new Case(page);
        String caseId2 = case1.createCase();
        case2.buildCase();

        case2.linkCase(caseId1, caseId2);
    }

    @RepeatedIfExceptionsTest
    void unlinkCasesTest() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();

        Case case1 = new Case(page);
        String caseId1 = case1.createCase();
        case1.buildCase();

        Case case2 = new Case(page);
        String caseId2 = case2.createCase();
        case2.buildCase();

        case2.linkCase(caseId1, caseId2);
        case2.unlinkCase(caseId1, caseId2);
    }
}
