package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ApplicationEvidence;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.model.TribunalDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.getDecisionDocs;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.getFinalDecisionDocs;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.removeDecisionDoc;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.removeFinalDecisionDoc;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.checkLists;
import static uk.gov.hmcts.sptribs.caseworker.util.OrderDocumentListUtil.getOrderDocuments;
import static uk.gov.hmcts.sptribs.caseworker.util.OrderDocumentListUtil.removeOrderDoc;


public final class DocumentListUtil {
    private DocumentListUtil() {

    }


    private static List<CaseworkerCICDocument> prepareList(CaseData data) {
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

    public static DynamicMultiSelectList prepareDocumentList(final CaseData data, boolean withCaseIssueFilters) {
        List<CaseworkerCICDocument> docList = prepareList(data);
        if (withCaseIssueFilters) {
            docList = filterCaseIssue(docList, data.getCaseIssue());
        }
        List<DynamicListElement> dynamicListElements = docList
            .stream()
            .map(doc -> DynamicListElement.builder().label(doc.getDocumentLink().getFilename()
                + "--" + doc.getDocumentLink().getUrl()).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .value(new ArrayList<>())
            .build();
    }

    private static List<CaseworkerCICDocument> filterCaseIssue(List<CaseworkerCICDocument> docList, CaseIssue caseIssue) {
        List<CaseworkerCICDocument> filteredList = new ArrayList<>();
        List<DocumentType> documentTypes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(caseIssue.getTribunalDocuments())) {
            for (TribunalDocuments tribunalDocuments : caseIssue.getTribunalDocuments()) {
                documentTypes.add(tribunalDocuments.getDocumentType());
            }
        }
        if (!CollectionUtils.isEmpty(caseIssue.getApplicationEvidences())) {
            for (ApplicationEvidence applicationEvidence : caseIssue.getApplicationEvidences()) {
                documentTypes.add(applicationEvidence.getDocumentType());
            }
        }
        for (CaseworkerCICDocument caseworkerCICDocument : docList) {
            if (!ObjectUtils.isEmpty(caseworkerCICDocument.getDocumentCategory())
                && documentTypes.contains(caseworkerCICDocument.getDocumentCategory())) {
                filteredList.add(caseworkerCICDocument);
            }
        }
        return filteredList;
    }

    private static List<CaseworkerCICDocument> getReinstateDocuments(CicCase cicCase) {
        List<CaseworkerCICDocument> reinstateDocList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getReinstateDocuments())) {
            for (ListValue<CaseworkerCICDocument> document : cicCase.getReinstateDocuments()) {
                reinstateDocList.add(document.getValue());
            }
        }
        return reinstateDocList;
    }

    private static List<CaseworkerCICDocument> getCaseDocs(CicCase cicCase) {
        List<CaseworkerCICDocument> caseDocs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getApplicantDocumentsUploaded())) {
            for (ListValue<CaseworkerCICDocument> document : cicCase.getApplicantDocumentsUploaded()) {
                caseDocs.add(document.getValue());
            }
        }
        return caseDocs;
    }

    private static List<CaseworkerCICDocument> getDocumentManagementDocs(CaseData caseData) {
        List<CaseworkerCICDocument> docManagementDocs = new ArrayList<>();
        if (null != caseData.getDocManagement() && !CollectionUtils.isEmpty(caseData.getDocManagement().getCaseworkerCICDocument())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getDocManagement().getCaseworkerCICDocument()) {
                docManagementDocs.add(document.getValue());
            }
        }
        return docManagementDocs;
    }

    private static List<CaseworkerCICDocument> getCloseCaseDocuments(CaseData caseData) {
        List<CaseworkerCICDocument> closeCaseDocs = new ArrayList<>();
        if (null != caseData.getCloseCase() && !CollectionUtils.isEmpty(caseData.getCloseCase().getDocuments())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getCloseCase().getDocuments()) {
                closeCaseDocs.add(document.getValue());
            }
        }
        return closeCaseDocs;
    }

    private static List<CaseworkerCICDocument> getHearingSummaryDocuments(CaseData caseData) {
        List<CaseworkerCICDocument> hearingSummaryDocs = new ArrayList<>();
        if (null != caseData.getListing() && null != caseData.getListing().getSummary()
            && !CollectionUtils.isEmpty(caseData.getListing().getSummary().getRecFile())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getListing().getSummary().getRecFile()) {
                hearingSummaryDocs.add(document.getValue());
            }
        }
        return hearingSummaryDocs;
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllDecisionDocuments(CaseData caseData) {
        return buildListValues(getDecisionDocs(caseData));
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllFinalDecisionDocuments(CaseData caseData) {
        return buildListValues(getFinalDecisionDocs(caseData));
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllOrderDocuments(CicCase cicCase) {
        return buildListValues(getOrderDocuments(cicCase));
    }

    private static List<ListValue<CaseworkerCICDocument>> buildListValues(List<CaseworkerCICDocument> docList) {
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

    public static CaseData removeEvaluatedListDoc(CaseData caseData, CaseData oldData) {
        removeDecisionDoc(caseData, oldData);
        removeFinalDecisionDoc(caseData, oldData);
        var cic = caseData.getCicCase();
        removeOrderDoc(cic, oldData.getCicCase());
        if (!CollectionUtils.isEmpty(oldData.getDocManagement().getCaseworkerCICDocument())
            && (CollectionUtils.isEmpty(caseData.getDocManagement().getCaseworkerCICDocument())
            || caseData.getDocManagement().getCaseworkerCICDocument().size()
            < oldData.getDocManagement().getCaseworkerCICDocument().size())) {
            checkLists(caseData, oldData.getDocManagement().getCaseworkerCICDocument(),
                caseData.getDocManagement().getCaseworkerCICDocument());
        }
        if (!CollectionUtils.isEmpty(oldData.getCloseCase().getDocuments())
            && (CollectionUtils.isEmpty(caseData.getCloseCase().getDocuments())
            || caseData.getCloseCase().getDocuments().size() < oldData.getCloseCase().getDocuments().size())) {
            checkLists(caseData, oldData.getCloseCase().getDocuments(), caseData.getCloseCase().getDocuments());
        }
        if (!CollectionUtils.isEmpty(oldData.getCicCase().getReinstateDocuments())
            && (CollectionUtils.isEmpty(caseData.getCicCase().getReinstateDocuments())
            || cic.getReinstateDocuments().size() < oldData.getCicCase().getReinstateDocuments().size())) {
            checkLists(caseData, oldData.getCicCase().getReinstateDocuments(), cic.getReinstateDocuments());
        }
        if (!CollectionUtils.isEmpty(oldData.getCicCase().getApplicantDocumentsUploaded())
            && (CollectionUtils.isEmpty(cic.getApplicantDocumentsUploaded())
            || cic.getApplicantDocumentsUploaded().size() < oldData.getCicCase().getApplicantDocumentsUploaded().size())) {
            checkLists(caseData, oldData.getCicCase().getApplicantDocumentsUploaded(), cic.getApplicantDocumentsUploaded());
        }
        if (!CollectionUtils.isEmpty(oldData.getListing().getSummary().getRecFile())
            && (CollectionUtils.isEmpty(caseData.getListing().getSummary().getRecFile())
            || caseData.getListing().getSummary().getRecFile().size() < oldData.getListing().getSummary().getRecFile().size())) {
            checkLists(caseData, oldData.getListing().getSummary().getRecFile(), caseData.getListing().getSummary().getRecFile());
        }
        return caseData;
    }

}
