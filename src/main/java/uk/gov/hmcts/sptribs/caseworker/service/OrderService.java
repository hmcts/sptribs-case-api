package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    List<DynamicListElement> dynamicListElements = new ArrayList<>();

    public DynamicList getOrderDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        List<ListValue<Order>> orderList = data.getCicCase().getOrderList();
        List<String> orders = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderList)) {
            for (ListValue<Order> order : orderList) {
                String draft = order.getId();
                orders.add(draft);
            }
            List<DynamicListElement> dynamicListElements = orders
                .stream()
                .sorted()
                .map(order -> DynamicListElement.builder().label(order).code(UUID.randomUUID()).build())
                .collect(Collectors.toList());

            return DynamicList
                .builder()
                .value(DynamicListElement.builder().label("order").code(UUID.randomUUID()).build())
                .listItems(dynamicListElements)
                .build();
        }
        return null;
    }

    public DynamicList getDraftOrderDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        List<ListValue<DraftOrderCIC>> draftList = data.getCicCase().getDraftOrderCICList();
        List<String> draftOrderList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(draftList)) {
            for (int i = 0; i < draftList.size(); i++) {
                DraftOrderCIC draftOrderCIC = draftList.get(i).getValue();
                String draft = i + "-" + draftOrderCIC.getAnOrderTemplate().getLabel();
                draftOrderList.add(draft);
            }

            List<DynamicListElement> dynamicListElements = draftOrderList
                .stream()
                .sorted()
                .map(draft -> DynamicListElement.builder().label(draft).code(UUID.randomUUID()).build())
                .collect(Collectors.toList());

            return DynamicList
                .builder()
                .value(DynamicListElement.builder().label("draft").code(UUID.randomUUID()).build())
                .listItems(dynamicListElements)
                .build();
        }
        return null;
    }


    public DynamicList getDraftOrderTemplatesDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        OrderTemplate orderTemplate = caseDetails.getData().getCicCase().getAnOrderTemplates();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ENGLISH);
        String draftOrderCreateDate = simpleformat.format(cal.getTime());

        String templateNamePlusCurrentDate = orderTemplate.getLabel() + " "
            + draftOrderCreateDate + "_draft.pdf";

        if (null != caseDetails.getData().getCicCase().getOrderTemplateDynamisList()) {
            dynamicListElements.add(DynamicListElement.builder().label(templateNamePlusCurrentDate).code(UUID.randomUUID()).build());
        } else {
            dynamicListElements = new ArrayList<>();
            dynamicListElements.add(DynamicListElement.builder().label(templateNamePlusCurrentDate).code(UUID.randomUUID()).build());
        }

        return DynamicList
            .builder()
            .value(DynamicListElement.builder().label("template")
                .code(UUID.randomUUID()).build())
            .listItems(dynamicListElements)
            .build();
    }


}
