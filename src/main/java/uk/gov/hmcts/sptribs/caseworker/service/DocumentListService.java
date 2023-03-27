package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentListService {

    private List<CaseworkerCICDocument> prepareList(CaseData data) {
        List<CaseworkerCICDocument> docList = new ArrayList<>();
        docList.addAll(getOrderDocuments(data.getCicCase()));
        docList.addAll(getCaseDocs(data.getCicCase()));
        docList.addAll(getReinstateDocuments(data.getCicCase()));
        docList.addAll(getDecisionDocs(data));
        docList.addAll(getFinalDecisionDocs(data));
        docList.addAll(getDocumentManagementDocs(data));
        docList.addAll(getCloseCaseDocuments(data));
        docList.addAll(getHearingSummaryDocuments(data));
        return docList;
    }


    public DynamicList prepareDocumentList(final CaseData data) {
        List<CaseworkerCICDocument> docList = prepareList(data);

        List<DynamicListElement> dynamicListElements = docList
            .stream()
            .sorted()
            .map(doc -> DynamicListElement.builder().label(doc.getDocumentLink().getFilename()).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }

    private List<CaseworkerCICDocument> getReinstateDocuments(CicCase cicCase) {
        List<CaseworkerCICDocument> reinstateDocList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getReinstateDocuments())) {
            for (ListValue<CaseworkerCICDocument> document : cicCase.getReinstateDocuments()) {
                reinstateDocList.add(document.getValue());
            }
        }
        return reinstateDocList;
    }

    private List<CaseworkerCICDocument> getCaseDocs(CicCase cicCase) {
        List<CaseworkerCICDocument> caseDocs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getApplicantDocumentsUploaded())) {
            for (ListValue<CaseworkerCICDocument> document : cicCase.getApplicantDocumentsUploaded()) {
                caseDocs.add(document.getValue());
            }
        }
        return caseDocs;
    }

    public List<CaseworkerCICDocument> getDecisionDocs(CaseData caseData) {
        List<CaseworkerCICDocument> decisionDocs = new ArrayList<>();
        if (null != caseData.getCaseIssueDecision()) {
            if (null != caseData.getCaseIssueDecision().getDecisionDocument()
                && null != caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink()) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                decisionDocs.add(doc);
            } else if (null != caseData.getCaseIssueDecision().getIssueDecisionDraft()
                && !ObjectUtils.isEmpty(caseData.getCaseIssueDecision().getIssueDecisionDraft().getFilename())) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(caseData.getCaseIssueDecision().getIssueDecisionDraft())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                decisionDocs.add(doc);
            }
        }
        return decisionDocs;
    }

    public List<CaseworkerCICDocument> getFinalDecisionDocs(CaseData caseData) {
        List<CaseworkerCICDocument> finalDecisionDocs = new ArrayList<>();
        if (null != caseData.getCaseIssueFinalDecision()) {
            if (null != caseData.getCaseIssueFinalDecision().getDocument()
                && null != caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink()) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                finalDecisionDocs.add(doc);
            } else if (null != caseData.getCaseIssueFinalDecision().getFinalDecisionDraft()
                && !ObjectUtils.isEmpty(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft().getFilename())) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                finalDecisionDocs.add(doc);
            }
        }
        return finalDecisionDocs;
    }

    private List<CaseworkerCICDocument> getDocumentManagementDocs(CaseData caseData) {
        List<CaseworkerCICDocument> docManagementDocs = new ArrayList<>();
        if (null != caseData.getDocManagement() && !CollectionUtils.isEmpty(caseData.getDocManagement().getCaseworkerCICDocument())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getDocManagement().getCaseworkerCICDocument()) {
                docManagementDocs.add(document.getValue());
            }
        }
        return docManagementDocs;
    }

    private List<CaseworkerCICDocument> getCloseCaseDocuments(CaseData caseData) {
        List<CaseworkerCICDocument> closeCaseDocs = new ArrayList<>();
        if (null != caseData.getCloseCase() && !CollectionUtils.isEmpty(caseData.getCloseCase().getDocuments())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getCloseCase().getDocuments()) {
                closeCaseDocs.add(document.getValue());
            }
        }
        return closeCaseDocs;
    }

    private List<CaseworkerCICDocument> getHearingSummaryDocuments(CaseData caseData) {
        List<CaseworkerCICDocument> hearingSummaryDocs = new ArrayList<>();
        if (null != caseData.getListing() && null != caseData.getListing().getSummary()
            && !CollectionUtils.isEmpty(caseData.getListing().getSummary().getRecFile())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getListing().getSummary().getRecFile()) {
                hearingSummaryDocs.add(document.getValue());
            }
        }
        return hearingSummaryDocs;
    }

    public List<CaseworkerCICDocument> getOrderDocuments(CicCase cicCase) {
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
                    for (ListValue<CICDocument> document : orderListValue.getValue().getUploadedFile()) {
                        if (null != document.getValue().getDocumentLink()) {
                            CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                                .documentLink(document.getValue().getDocumentLink())
                                .documentEmailContent(document.getValue().getDocumentEmailContent())
                                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                                .build();
                            orderList.add(doc);
                        }
                    }
                }
            }
        }
        return orderList;
    }

    public List<ListValue<CaseworkerCICDocument>> getAllDecisionDocuments(CaseData caseData) {
        return buildListValues(getDecisionDocs(caseData));
    }

    public List<ListValue<CaseworkerCICDocument>> getAllFinalDecisionDocuments(CaseData caseData) {
        return buildListValues(getFinalDecisionDocs(caseData));
    }

    public List<ListValue<CaseworkerCICDocument>> getAllOrderDocuments(CicCase cicCase) {
        return buildListValues(getOrderDocuments(cicCase));
    }

    private List<ListValue<CaseworkerCICDocument>> buildListValues(List<CaseworkerCICDocument> docList) {
        List<ListValue<CaseworkerCICDocument>> newList = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        for (CaseworkerCICDocument doc : docList) {
            var listValue = ListValue
                .<CaseworkerCICDocument>builder()
                .value(doc)
                .build();

            newList.add(0, listValue);
            newList.forEach(
                document -> document.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return newList;
    }
}
