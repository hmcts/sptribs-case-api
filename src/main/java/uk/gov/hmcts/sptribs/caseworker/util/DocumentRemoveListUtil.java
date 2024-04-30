package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.removeDecisionDoc;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.removeFinalDecisionDoc;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.checkLists;
import static uk.gov.hmcts.sptribs.caseworker.util.OrderDocumentListUtil.removeOrderDoc;


public final class DocumentRemoveListUtil {

    private DocumentRemoveListUtil() {

    }

    public static CaseData removeEvaluatedListDoc(CaseData caseData, CaseData oldData) {
        removeDecisionDoc(caseData, oldData);
        removeFinalDecisionDoc(caseData, oldData);
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

}
