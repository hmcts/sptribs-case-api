package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.EMPTY_DOCUMENT;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.addToRemovedDocuments;

public final class OrderDocumentListUtil {

    private OrderDocumentListUtil() {

    }

    public static List<CaseworkerCICDocument> getOrderDocuments(CicCase cicCase) {
        List<CaseworkerCICDocument> orderList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getOrderList())) {
            for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
                if (orderListValue.getValue().getDraftOrder() != null
                    && orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument() != null
                    && !ObjectUtils.isEmpty(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument().getFilename())) {
                    Document templateGeneratedDoc = orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument();
                    CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                        .documentLink(templateGeneratedDoc)
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
            if (document.getValue().getDocumentLink() != null) {
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


    public static CicCase removeOrderDoc(CicCase cicCase, CicCase oldCicCase) {
        List<ListValue<CaseworkerCICDocument>> wholeOrderDocList = DocumentListUtil.getAllOrderDocuments(oldCicCase);

        if (wholeOrderDocList.size() > cicCase.getOrderDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : wholeOrderDocList) {
                if (!cicCase.getOrderDocumentList().contains(cicDocumentListValue)) {
                    for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
                        if (orderListValue.getValue().getDraftOrder() != null
                            && cicDocumentListValue.getValue().getDocumentLink()
                            .equals(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument())) {
                            orderListValue.getValue().getDraftOrder().setTemplateGeneratedDocument(null);
                        } else {
                            manageUploadedFiles(orderListValue, cicDocumentListValue);
                        }
                    }
                    addToRemovedDocuments(cicCase, cicDocumentListValue.getValue());
                }
            }
        }
        return cicCase;
    }

    public static void manageUploadedFiles(ListValue<Order> orderListValue, ListValue<CaseworkerCICDocument> cicDocumentListValue) {
        if (!CollectionUtils.isEmpty(orderListValue.getValue().getUploadedFile())) {
            for (int i = 0; i < orderListValue.getValue().getUploadedFile().size(); i++) {
                ListValue<CICDocument> file = orderListValue.getValue().getUploadedFile().get(i);
                if (file.getValue().getDocumentLink() != null && file.getValue().getDocumentLink()
                    .equals(cicDocumentListValue.getValue().getDocumentLink())) {
                    orderListValue.getValue().getUploadedFile().get(i).setValue(EMPTY_DOCUMENT);

                }
            }
        }
    }

    public static void removeOrderDraftAndCICDocument(CaseData caseData, CaseworkerCICDocument cicDocument) {
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getOrderDocumentList())) {
            CicCase cicCase = caseData.getCicCase();
            List<CaseworkerCICDocument> orderDocumentList = cicCase.getOrderDocumentList().stream().map(ListValue::getValue).toList();
            if (orderDocumentList.contains(cicDocument)) {
                for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
                    if (orderListValue.getValue().getDraftOrder() != null
                        && cicDocument.getDocumentLink().equals(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument())) {
                        orderListValue.getValue().getDraftOrder().setTemplateGeneratedDocument(null);
                    } else {
                        if (!CollectionUtils.isEmpty(orderListValue.getValue().getUploadedFile())) {
                            for (int i = 0; i < orderListValue.getValue().getUploadedFile().size(); i++) {
                                ListValue<CICDocument> file = orderListValue.getValue().getUploadedFile().get(i);
                                if (file.getValue().getDocumentLink() != null
                                    && file.getValue().getDocumentLink().equals(cicDocument.getDocumentLink())) {
                                    orderListValue.getValue().getUploadedFile().get(i).setValue(EMPTY_DOCUMENT);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
