package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate.CIC6GeneralDirections;

public class SendOrdersTests extends Base {
    Case newCase;

    @Test
    void sendAnExistingDraftOrder() {
        createCase();
        newCase.buildCase();
        newCase.createDraft(CIC6GeneralDirections);
        newCase.selectAnExistingDraftOrderAndSend();
    }

    @Test
    void uploadAndSendOrder() {
        createCase();
        newCase.buildCase();
        newCase.uploadAndSendOrder(now().plusMonths(1));
    }

    private void createCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsStTest1User();
        newCase = new Case(page);
        newCase.createCase();
    }
}
