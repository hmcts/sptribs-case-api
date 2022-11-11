package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.SendOrder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderOrderIssuingSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectOrderIssuingType")
            .label("selectOrderIssuingType", "<h1>Select order\n</h1>")
            .complex(CaseData::getSendOrder,"","","")
            .mandatory(SendOrder::getOrderIssuingType)
            .done();
    }
}
