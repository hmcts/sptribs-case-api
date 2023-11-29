package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.gov.hmcts.sptribs.e2e.enums.CaseParty;

import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.ApplicantEmail;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.RepresentativePost;
import static uk.gov.hmcts.sptribs.e2e.enums.CasePartyContactPreference.SubjectEmail;

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
        newCase.createCase(SubjectEmail, ApplicantEmail, RepresentativePost);
        newCase.buildCase();
        newCase.createFlagForParties(CaseParty.Subject, CaseParty.Applicant, CaseParty.Representative);
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
        newCase.updateCaseLevelFlags();
    }
}









