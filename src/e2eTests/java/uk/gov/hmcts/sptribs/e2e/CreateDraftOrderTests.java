package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;

import static uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate.CIC6GeneralDirections;
import static uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate.CIC7MEDmiReports;

public class CreateDraftOrderTests extends Base {

    @RepeatedIfExceptionsTest
    void createDraftOrder() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase("representative", "applicant");
        newCase.buildCase();
        newCase.createDraft(CIC6GeneralDirections);
        newCase.createDraft(CIC7MEDmiReports);
    }
}
