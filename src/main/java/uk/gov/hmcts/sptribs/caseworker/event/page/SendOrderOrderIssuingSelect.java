package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderOrderIssuingSelect implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSelectOrderIssuingType", this::midEvent)
            .pageLabel("Select order")
            .label("LabelCaseworkerSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getOrderIssuingDynamicRadioList)
                .readonly(CicCase::getOrderIssuingType, "LabelCaseworkerSendOrderSelectOrderIssuingType=\"HIDDEN\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        CicCase cicCase = caseData.getCicCase();

        if (cicCase.getOrderIssuingDynamicRadioList() != null) {
            OrderIssuingType selectedValue = DynamicListUtil.getEnumFromUuid(
                cicCase.getOrderIssuingDynamicRadioList().getValueCode(),
                OrderIssuingType.class);
            cicCase.setOrderIssuingType(selectedValue);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
