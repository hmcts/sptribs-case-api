package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class OrderService {
    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;

    @Autowired
    private HttpServletRequest request;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                .listItems(dynamicListElements)
                .build();
        }
        return null;
    }

    public DynamicList getDraftOrderTemplatesDynamicList(final OrderTemplate orderTemplate, DynamicList orderTemplateDynamicList) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ENGLISH);
        String draftOrderCreateDate = simpleformat.format(cal.getTime());

        String templateNamePlusCurrentDate = orderTemplate.getLabel() + " "
            + draftOrderCreateDate + "_draft.pdf";

        if (null != orderTemplateDynamicList) {
            dynamicListElements = orderTemplateDynamicList.getListItems();
            dynamicListElements.add(DynamicListElement.builder().label(templateNamePlusCurrentDate).code(UUID.randomUUID()).build());
        } else {
            dynamicListElements.add(DynamicListElement.builder().label(templateNamePlusCurrentDate).code(UUID.randomUUID()).build());
        }

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }

    public CaseData generateOrderFile(CaseData caseData, Long caseId) {
        var template = caseData.getCicCase().getOrderTemplate();
        String subjectName = caseData.getCicCase().getFullName();
        final String filename = "Order-[" + subjectName + "]-" + LocalDateTime.now().format(formatter);

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            previewDraftOrderTemplateContent.apply(caseData, caseId),
            caseId,
            caseData.getCicCase().getOrderTemplate().getId(),
            LanguagePreference.ENGLISH,
            filename,
            request
        );

        caseData.getCicCase().setOrderTemplate(template);
        caseData.getCicCase().setOrderTemplateIssued(generalOrderDocument);
        return caseData;
    }


}
