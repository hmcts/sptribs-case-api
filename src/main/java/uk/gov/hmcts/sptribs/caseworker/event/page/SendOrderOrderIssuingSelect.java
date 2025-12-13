package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderOrderIssuingSelect implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSelectOrderIssuingType")
            .pageLabel("Select order")
            .label("LabelCaseworkerSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getOrderIssuingType)
                .done();
    }
}
