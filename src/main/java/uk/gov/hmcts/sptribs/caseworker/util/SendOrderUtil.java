package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SendOrderUtil {

    private SendOrderUtil() {
        // Utility class
    }

    public static void updateCicCaseOrderList(CaseData caseData, Order order) {
        if (CollectionUtils.isEmpty(caseData.getCicCase().getOrderList())) {
            List<ListValue<Order>> listValues = new ArrayList<>();

            ListValue<Order> listValue = ListValue
                    .<Order>builder()
                    .id("1")
                    .value(order)
                    .build();

            listValues.add(listValue);

            caseData.getCicCase().setOrderList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<Order> listValue = ListValue
                    .<Order>builder()
                    .value(order)
                    .build();

            // always add new order as first element so that it is displayed on top
            caseData.getCicCase().getOrderList().addFirst(listValue);

            caseData.getCicCase().getOrderList().forEach(
                    orderListValue -> orderListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
    }

}
