package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;

import java.time.LocalDate;

import static uk.gov.hmcts.sptribs.e2e.enums.DraftOrderTemplate.CIC6GeneralDirections;

public class ManageOrderDueDateTests extends Base {
    Case newCase;

    @Disabled
    void manageOrderDueDate() {
        createCase();
        LocalDate date = LocalDate.now();
        createAndSendOrderWithDueDate(date.plusMonths(1));
        newCase.mangeOrderDueDateAmendDueDate(date.plusMonths(2));
    }

    private void createAndSendOrderWithDueDate(LocalDate localDate) {
        newCase.buildCase();
        newCase.createDraft(CIC6GeneralDirections);
        newCase.selectAnExistingDraftOrderAndSend(localDate);
    }

    private void createCase() {
        Page page = getPage();
        Login login = new Login(page);
        login.loginAsCaseWorker();
        newCase = new Case(page);
        newCase.createCase();
    }
}
