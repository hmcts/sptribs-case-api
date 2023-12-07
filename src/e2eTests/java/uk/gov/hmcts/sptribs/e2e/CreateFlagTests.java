package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.gov.hmcts.sptribs.e2e.enums.CaseParties;

public class CreateFlagTests extends Base {

    @RepeatedIfExceptionsTest
    public void caseWorkedShouldBeAbleToCreateCaseLevelFlag() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsLegalOfficer();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.createCaseLevelFlag();
    }

    @RepeatedIfExceptionsTest
    public void caseWorkedShouldBeAbleToCreatePartyLevelFlag() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsLegalOfficer();
        Case newCase = new Case(page);
        newCase.createCase(CaseParties.Subject.label, CaseParties.Applicant.label, CaseParties.Representative.label);
        newCase.buildCase();
        newCase.createFlagForParties(CaseParties.Subject, CaseParties.Applicant, CaseParties.Representative);
    }

    @RepeatedIfExceptionsTest
    public void caseWorkerShouldBeAbleToDoManageFlags() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsLegalOfficer();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        newCase.createCaseLevelFlag();
        newCase.updateCaseLevelFlag();
    }
}









