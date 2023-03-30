package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

public final class OrderDocumentListUtil {

    private OrderDocumentListUtil() {

    }

    public static List<CaseworkerCICDocument> getOrderDocuments(CicCase cicCase) {
        List<CaseworkerCICDocument> orderList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getOrderList())) {
            for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
                if (null != orderListValue.getValue().getDraftOrder()
                    && null != orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument()
                    && !ObjectUtils.isEmpty(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument().getFilename())) {
                    CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                        .documentLink(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument())
                        .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                        .build();
                    orderList.add(doc);
                } else if (!CollectionUtils.isEmpty(orderListValue.getValue().getUploadedFile())) {
                    orderList.addAll(checkUploadedFile(orderListValue));
                }
            }
        }
        return orderList;
    }


    private static List<CaseworkerCICDocument> checkUploadedFile(ListValue<Order> orderListValue) {
        List<CaseworkerCICDocument> orderUploadList = new ArrayList<>();
        for (ListValue<CICDocument> document : orderListValue.getValue().getUploadedFile()) {
            if (null != document.getValue().getDocumentLink()) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(document.getValue().getDocumentLink())
                    .documentEmailContent(document.getValue().getDocumentEmailContent())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                orderUploadList.add(doc);
            }
        }
        return orderUploadList;
    }
}
