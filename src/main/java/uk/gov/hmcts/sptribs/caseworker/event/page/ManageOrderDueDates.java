package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.SendOrder;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;


public class ManageOrderDueDates implements CcdPageConfiguration {

    @Autowired
    OrderService orderService;
    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("dueDatesForOrders")
            .label("dueDatesForOrder", "<h1>Select an order\n</h1>")
            .complex(CaseData::getDraftOrderCIC)
            .mandatory(DraftOrderCIC::getOrderTemplate, "")
            .optional(DraftOrderCIC::getMainContentForGeneralDirections, "draftOrderTemplate = \"GeneralDirections\"")
            .optional(DraftOrderCIC::getMainContentForDmiReports, "draftOrderTemplate = \"Medical Evidence - DMI Reports\"")
            .done()
           // .complex(CaseData::getSendOrder, "", "", "")
           //.optional(CaseData::getDueDate)
            .complex(CaseData::getSendOrder, "", "", "")
            .optional(SendOrder::getDueDates)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var dueDatesList = details.getData();
        var duedates = dueDatesList.getSendOrder().getDueDates();
        DynamicList dueDates = orderService.getDueDates(duedates);
//        var caseData = details.getData();
//        var draftOrder = caseData.getSendOrder().getDueDates();
//
//        if (isEmpty(draftOrder)) {
//            List<ListValue<DateModel>> listValues = new ArrayList<>();
//
//            var listValue = ListValue
//                .<DateModel>builder()
//                .id("1")
//                .value(caseData.getSendOrder().getDueDates().get(0).getValue())
//                .build();
//
//            listValues.add(listValue);
//
//            caseData.getSendOrder().setDueDates(listValues);
//        }else {
//            AtomicInteger listValueIndex = new AtomicInteger(0);
//            var listValue = ListValue
//                .<DateModel>builder()
//                .value(caseData.getSendOrder().getDueDates().get(0).getValue())
//                .build();
//
//            List<ListValue<DateModel>> dueDates = caseData.getSendOrder().getDueDates();
//            dueDates.add(0, listValue); // always add new note as first element so that it is displayed on top
//
////            caseData.getSendOrder().getDueDates().forEach(
////                caseData.getSendOrder() ->  caseData.getSendOrder().setId(String.valueOf(listValueIndex.incrementAndGet()))
////            );
//
//        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .build();

    }


}
