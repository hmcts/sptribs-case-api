package uk.gov.hmcts.sptribs.e2e;

import org.junit.jupiter.api.Test;

public class StayCase extends Base {
    @Test
    public void caseWorkerShouldBeAbleToAddStayToCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase();
        newCase.buildCase();
        newCase.addStayToCase();

    }
}
