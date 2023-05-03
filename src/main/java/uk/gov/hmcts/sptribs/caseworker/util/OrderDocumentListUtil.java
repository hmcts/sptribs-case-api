package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
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
                if (null != orderListValue.getValue().getDraftOrder()
                    && null != orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument()
                    && !ObjectUtils.isEmpty(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument().getFilename())) {
                    Document templateGeneratedDoc = orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument();
                    DocumentType documentCategory = (null != templateGeneratedDoc.getCategoryId())
                        ? DocumentType.fromCategory(templateGeneratedDoc.getCategoryId()).get()
                        : DocumentType.TRIBUNAL_DIRECTION;
                    CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                        .documentLink(templateGeneratedDoc)
                        .documentCategory(documentCategory)
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
                Document documentLink = document.getValue().getDocumentLink();
                DocumentType documentCategory = (null != documentLink.getCategoryId())
                    ? DocumentType.fromCategory(documentLink.getCategoryId()).get()
                    : DocumentType.TRIBUNAL_DIRECTION;
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(document.getValue().getDocumentLink())
                    .documentEmailContent(document.getValue().getDocumentEmailContent())
                    .documentCategory(documentCategory)
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
                        if (null != orderListValue.getValue().getDraftOrder()
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
                if (null != file.getValue().getDocumentLink() && file.getValue().getDocumentLink()
                    .equals(cicDocumentListValue.getValue().getDocumentLink())) {
                    orderListValue.getValue().getUploadedFile().get(i).setValue(EMPTY_DOCUMENT);

                }
            }
        }
    }

    public static void updateOrderTypeDocumentList(CicCase cicCase, CaseworkerCICDocument selectedDocument) {
        for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
            DraftOrderCIC draftOrderCIC = orderListValue.getValue().getDraftOrder();
            if (null != draftOrderCIC
                && selectedDocument.getDocumentLink().getUrl().equals(draftOrderCIC.getTemplateGeneratedDocument().getUrl())) {
                draftOrderCIC.getTemplateGeneratedDocument().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
            } else {
                amendUploadedFile(orderListValue, selectedDocument);
            }
        }
    }

    private static void amendUploadedFile(ListValue<Order> orderListValue, CaseworkerCICDocument selectedDocument) {
        if (!CollectionUtils.isEmpty(orderListValue.getValue().getUploadedFile())) {
            for (ListValue<CICDocument> file : orderListValue.getValue().getUploadedFile()) {
                if (null != file.getValue().getDocumentLink() && file.getValue().getDocumentLink()
                    .equals(selectedDocument.getDocumentLink())) {
                    file.getValue().getDocumentLink().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
                }
            }
        }
    }

}
