package uk.gov.hmcts.sptribs.e2e;

import org.junit.jupiter.api.Test;

import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC10StrikeOutWarning;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC13ProFormaSummons;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC6GeneralDirections;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC7MEDmiReports;
import static uk.gov.hmcts.sptribs.e2e.DraftOrderTemplate.CIC8MEJointInstruction;

public class CreateDraftOrder extends Base {

    @Test
    void createDraftOrder() {
        new Login().loginAsStTest1User();
        Case newCase = new Case();
        newCase.createCase("representative", "applicant");
        newCase.buildCase();
        newCase.createDraft(CIC6GeneralDirections);
        newCase.createDraft(CIC7MEDmiReports);
        newCase.createDraft(CIC8MEJointInstruction);
        newCase.createDraft(CIC10StrikeOutWarning);
        newCase.createDraft(CIC13ProFormaSummons);
    }
}
