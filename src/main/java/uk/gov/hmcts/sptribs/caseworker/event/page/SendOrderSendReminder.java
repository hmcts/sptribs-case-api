package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderSendReminder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSendReminder")
            .pageLabel("Select order")
            .label("LabelCaseworkerSendOrderSendReminder", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getOrderReminderYesOrNo)
            .mandatory(CicCase::getOrderReminderDays, "cicCaseOrderReminderYesOrNo = \"Yes\"")
            .done();
    }
}
