package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;

import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

public class LinkCaseTests extends Base {

    @RepeatedIfExceptionsTest
    void linkTwoCasesTogetherTest() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsLegalOfficer();

        Case case1 = new Case(page);
        String caseId1 = createAndBuild(case1, page);

        Case case2 = new Case(page);
        String caseId2 = createAndBuild(case2, page);

        case2.linkCase(caseId1, caseId2);
    }

    @RepeatedIfExceptionsTest
    void unlinkCasesTest() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsLegalOfficer();

        Case case1 = new Case(page);
        String caseId1 = createAndBuild(case1, page);

        Case case2 = new Case(page);
        String caseId2 = createAndBuild(case2, page);

        case2.linkCase(caseId1, caseId2);
        case2.unlinkCase(caseId1, caseId2);
    }

    private String createAndBuild(Case newCase, Page page) {
        newCase.createCase();
        newCase.buildCase();
        Matcher m = compile("\\d+").matcher(page.url());
        String caseId1 = null;
        if (m.find()) {
            caseId1 = m.group(0);
        }
        return caseId1;
    }
}
