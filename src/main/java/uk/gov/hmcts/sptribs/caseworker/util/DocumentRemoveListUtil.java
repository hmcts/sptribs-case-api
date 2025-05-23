package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.addDecisionDocumentsForRemoval;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.addFinalDecisionDocumentsForRemoval;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.checkLists;
import static uk.gov.hmcts.sptribs.caseworker.util.OrderDocumentListUtil.removeOrderDoc;


public final class DocumentRemoveListUtil {

    private DocumentRemoveListUtil() {

    }

    public static CaseData setDocumentsListForRemoval(CaseData caseData, CaseData oldData) {
        addDecisionDocumentsForRemoval(caseData, oldData);
        addFinalDecisionDocumentsForRemoval(caseData, oldData);
        final CicCase cic = caseData.getCicCase();
        removeOrderDoc(cic, oldData.getCicCase());
        if (!CollectionUtils.isEmpty(oldData.getAllDocManagement().getCaseworkerCICDocument())
            && (CollectionUtils.isEmpty(caseData.getAllDocManagement().getCaseworkerCICDocument())
            || caseData.getAllDocManagement().getCaseworkerCICDocument().size()
            < oldData.getAllDocManagement().getCaseworkerCICDocument().size())) {
            checkLists(caseData, oldData.getAllDocManagement().getCaseworkerCICDocument(),
                caseData.getAllDocManagement().getCaseworkerCICDocument());
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

        if (!CollectionUtils.isEmpty(oldData.getLatestCompletedHearing().getSummary().getRecFile())
            && (CollectionUtils.isEmpty(caseData.getLatestCompletedHearing().getSummary().getRecFile())
            || caseData.getLatestCompletedHearing().getSummary().getRecFile().size()
            < oldData.getLatestCompletedHearing().getSummary().getRecFile().size())) {
            checkLists(caseData, oldData.getLatestCompletedHearing().getSummary().getRecFile(),
                caseData.getLatestCompletedHearing().getSummary().getRecFile());
        }

        return caseData;
    }

    public static void removeDocuments(CaseData caseData) {
        List<String> documentNamesToRemove = caseData.getCicCase().getSelectRemoveDocumentList().getValue().stream().map(DynamicListElement::getLabel).toList();
        List<ListValue<CaseworkerCICDocument>> allDocuments = DocumentListUtil.getAllCaseDocuments(caseData);
        List<CaseworkerCICDocument> documentsToRemove = allDocuments.stream().map(ListValue::getValue).filter(document -> {
            String documentLabel = document.getDocumentCategory().getLabel() + "--" + document.getDocumentLink().getFilename();
            return documentNamesToRemove.contains(documentLabel);

        }).toList();

        List<ListValue<CaseworkerCICDocument>> hearingRecordsList = caseData.getHearingList()
            .stream()
            .map(ListValue::getValue)
            .filter(hearing ->(hearing.getSummary() != null) && !CollectionUtils.isEmpty(hearing.getSummary().getRecFile()))
            .map(Listing::getSummary)
            .flatMap(hearingSummary -> hearingSummary.getRecFile().stream())
            .toList();

        if (!documentsToRemove.isEmpty()) {
            for (CaseworkerCICDocument cicDocument : documentsToRemove) {
                DecisionDocumentListUtil.removeDecisionDraftAndCICDocument(caseData, cicDocument);
                DecisionDocumentListUtil.removeFinalDecisionDraftAndCICDocument(caseData, cicDocument);
                OrderDocumentListUtil.removeOrderDraftAndCICDocument(caseData, cicDocument);

                removeFromLists(cicDocument,
                    caseData.getCicCase().getRemovedDocumentList(),
                    caseData.getCicCase().getApplicantDocumentsUploaded(),
                    caseData.getAllDocManagement().getCaseworkerCICDocument(),
                    caseData.getCloseCase().getDocuments(),
                    hearingRecordsList
                );
            }
        }
    }

    @SafeVarargs
    private static void removeFromLists(
            CaseworkerCICDocument target,
            List<ListValue<CaseworkerCICDocument>>... lists
    ) {
        for (List<ListValue<CaseworkerCICDocument>> list : lists) {
        if (!CollectionUtils.isEmpty(list)) {
                list.removeIf(entry -> entry.getValue().equals(target));
            }
        }
    }
}
