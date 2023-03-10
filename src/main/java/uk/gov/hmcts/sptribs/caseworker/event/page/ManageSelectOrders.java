package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getId;


public class ManageSelectOrders implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerAmendDueDateSelectOrder", this::midEvent)
            .pageLabel("Select order")
            .label("LabelCaseworkerAmendDueDateSelectOrder", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getOrderDynamicList)
            .done();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final List<String> errors = new ArrayList<>();
        String selectedOrder = data.getCicCase().getOrderDynamicList().getValue().getLabel();
        String id = getId(selectedOrder);
        var orderList = data.getCicCase().getOrderList();
        for (int i = 0; i < orderList.size(); i++) {
            if (null != id && id.equals(orderList.get(i).getId())) {
                cicCase.setOrderDueDates(orderList.get(i).getValue().getDueDateList());
                break;
            }
        }
        if (StringUtils.isBlank(selectedOrder)) {
            errors.add("Please select and order to manage");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
