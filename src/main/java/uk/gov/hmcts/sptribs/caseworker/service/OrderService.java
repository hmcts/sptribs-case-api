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
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DRAFT;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SEMICOLON;

@Service
@Slf4j
public class OrderService {
    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;

    @Autowired
    private HttpServletRequest request;

    public DynamicList getOrderDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        List<ListValue<Order>> orderList = data.getCicCase().getOrderList();
        List<String> orders = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderList)) {
            for (ListValue<Order> order : orderList) {
                if (null != order.getValue().getDraftOrder()) {
                    String item = order.getId() + EventConstants.HYPHEN
                        + order.getValue().getDraftOrder().getDraftOrderContentCIC().getOrderTemplate();
                    orders.add(item);
                } else {
                    String item = order.getId() + EventConstants.HYPHEN
                        + order.getValue().getUploadedFile().get(0).getValue().getDocumentLink().getFilename();
                    orders.add(item);
                }
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

    public CaseData generateOrderFile(CaseData caseData, Long caseId, String date) {
        String subjectName = caseData.getCicCase().getFullName();
        final String filename = DRAFT + SEMICOLON + "Order" + DOUBLE_HYPHEN + "[" + subjectName + "]" + DOUBLE_HYPHEN + date;

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            previewDraftOrderTemplateContent.apply(caseData, caseId),
            caseId,
            caseData.getDraftOrderContentCIC().getOrderTemplate().getId(),
            LanguagePreference.ENGLISH,
            filename,
            request
        );

        caseData.getCicCase().setOrderTemplateIssued(generalOrderDocument);
        return caseData;
    }


}
