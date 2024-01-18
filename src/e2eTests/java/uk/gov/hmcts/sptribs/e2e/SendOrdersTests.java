package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import io.github.artsok.RepeatedIfExceptionsTest;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate.CIC6GeneralDirections;

public class SendOrdersTests extends Base {

    @RepeatedIfExceptionsTest
    public void sendAnExistingDraftOrder() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);
        newCase.createDraft(CIC6GeneralDirections);
        newCase.selectAnExistingDraftOrderAndSend();
    }

    @RepeatedIfExceptionsTest
    public void uploadAndSendOrder() {
        Page page = getPage();
        Case newCase = createAndBuildCase(page);
        newCase.uploadAndSendOrder(now().plusMonths(1));
    }

    private Case createAndBuildCase(Page page) {
        Login login = new Login(page);
        login.loginAsCaseWorker();
        Case newCase = new Case(page);
        newCase.createCase();
        newCase.buildCase();
        return newCase;
    }
}
