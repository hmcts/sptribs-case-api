package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderOrderIssuingSelect implements CcdPageConfiguration {

    private static final String SHOW_ON_CREATE_AND_SEND = "currentEvent=\"create-and-send-order\"";
    private static final String HIDE_CURRENT_EVENT_FIELD = "cicCaseCreateAndSendIssuingTypes=\"NONE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSelectOrderIssuingType")
            .pageLabel("Select order")
            .label("LabelCaseworkerSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getOrderIssuingType)
                .readonly(CicCase::getCreateAndSendIssuingTypes, SHOW_ON_CREATE_AND_SEND)
                .done()
            .readonly(CaseData::getCurrentEvent, HIDE_CURRENT_EVENT_FIELD);
    }
}
