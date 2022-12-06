package uk.gov.hmcts.sptribs.caseworker.event.page;


import uk.gov.hmcts.sptribs.caseworker.model.SendOrder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderSendReminder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("sendOrderSendReminder")
            .pageLabel("Select order")
            .complex(CaseData::getSendOrder)
            .mandatory(SendOrder::getYesOrNo)
            .mandatory(SendOrder::getReminderDays, "sendOrderYesOrNo = \"Yes\"")
            .done();
    }
}
