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
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSelectOrderIssuingType", this::midEvent)
            .pageLabel("Select order")
            .label("LabelCaseworkerSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getOrderIssuingDynamicRadioList)
                .readonly(CicCase::getOrderIssuingType, "LabelCaseworkerSendOrderSelectOrderIssuingType=\"HIDDEN\"")
                .done();
    }

    public <T extends CaseData> AboutToStartOrSubmitResponse<T, State> midEvent(CaseDetails<T, State> details,
                                                                  CaseDetails<T, State> detailsBefore) {
        T caseData = details.getData();
        CicCase cicCase = caseData.getCicCase();

        if (cicCase.getOrderIssuingDynamicRadioList() != null) {
            OrderIssuingType selectedValue = DynamicListUtil.getEnumFromUuid(
                cicCase.getOrderIssuingDynamicRadioList().getValueCode(),
                OrderIssuingType.class);
            cicCase.setOrderIssuingType(selectedValue);
        }

        return AboutToStartOrSubmitResponse.<T, State>builder()
            .data(caseData)
            .build();
    }
}
