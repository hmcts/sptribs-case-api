package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC10StrikeOutWarning;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC13ProFormaSummons;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC6GeneralDirections;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC7MEDmiReports;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC8MEJointInstruction;

public class CreateDraftOrderTests extends Base {

    @Test
    void createDraftOrder() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        Case newCase = new Case(page);
        newCase.createCase("representative", "applicant");
        newCase.buildCase();
        newCase.createDraft(CIC6GeneralDirections);
        newCase.createDraft(CIC7MEDmiReports);
        newCase.createDraft(CIC8MEJointInstruction);
        newCase.createDraft(CIC10StrikeOutWarning);
        newCase.createDraft(CIC13ProFormaSummons);
    }
}
