package uk.gov.hmcts.sptribs.e2e;

import org.junit.jupiter.api.Test;

public class StayCase extends Base {
    @Test
    public void caseworkerShouldBeAbleToAddAStayToCase() {
        Login login = new Login();
        login.loginAsStTest1User();
        CreateCase createcase = new CreateCase();
        createcase.createACase();
    }
}
