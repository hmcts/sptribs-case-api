package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
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
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        pageBuilder.page("caseworkerAmendDueDateSelectOrder", this::midEvent)
            .pageLabel("Select order")
            .label("LabelCaseworkerAmendDueDateSelectOrder", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getOrderDynamicList)
            .done();
    }

    public <T extends CaseData> AboutToStartOrSubmitResponse<T, State> midEvent(CaseDetails<T, State> details,
                                                                  CaseDetails<T, State> detailsBefore) {

        final T data = details.getData();
        final List<String> errors = new ArrayList<>();
        final String selectedOrder = data.getCicCase().getOrderDynamicList().getValue().getLabel();
        final String id = getId(selectedOrder);
        final List<ListValue<Order>> orderList = data.getCicCase().getOrderList();

        if (CollectionUtils.isNotEmpty(orderList)) {
            for (ListValue<Order> orderListValue : orderList) {
                if (id != null && id.equals(orderListValue.getId())) {
                    data.setOrderDueDates(orderListValue.getValue().getDueDateList());
                    break;
                }
            }
        }
        if (CollectionUtils.isEmpty(orderList) || StringUtils.isBlank(selectedOrder)) {
            errors.add("Please select an order to manage");
        }

        return AboutToStartOrSubmitResponse.<T, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
