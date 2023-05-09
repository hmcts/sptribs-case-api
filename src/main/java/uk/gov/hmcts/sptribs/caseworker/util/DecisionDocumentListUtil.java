package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.EMPTY_DOCUMENT;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.addToRemovedDocuments;

public  final class DecisionDocumentListUtil {

    private DecisionDocumentListUtil() {

    }

    public static List<CaseworkerCICDocument> getDecisionDocs(CaseData caseData) {
        List<CaseworkerCICDocument> decisionDocs = new ArrayList<>();
        if (null != caseData.getCaseIssueDecision()) {
            if (null != caseData.getCaseIssueDecision().getDecisionDocument()
                && null != caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink()) {
                CICDocument decisionDocument = caseData.getCaseIssueDecision().getDecisionDocument();
                DocumentType documentCategory = (null != decisionDocument.getDocumentLink().getCategoryId())
                    ? DocumentType.fromCategory(decisionDocument.getDocumentLink().getCategoryId()).get()
                    : DocumentType.TRIBUNAL_DIRECTION;
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentEmailContent(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentEmailContent())
                    .documentLink(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())
                    .documentCategory(documentCategory)
                    .build();
                decisionDocs.add(doc);
            }

            if (null != caseData.getCaseIssueDecision().getIssueDecisionDraft()
                && !ObjectUtils.isEmpty(caseData.getCaseIssueDecision().getIssueDecisionDraft().getFilename())) {
                Document decisionDraft = caseData.getCaseIssueDecision().getIssueDecisionDraft();
                DocumentType documentCategory = (null != decisionDraft.getCategoryId())
                    ? DocumentType.fromCategory(decisionDraft.getCategoryId()).get()
                    : DocumentType.TRIBUNAL_DIRECTION;
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(caseData.getCaseIssueDecision().getIssueDecisionDraft())
                    .documentCategory(documentCategory)
                    .build();
                decisionDocs.add(doc);
            }
        }
        return decisionDocs;
    }

    public static List<CaseworkerCICDocument> getFinalDecisionDocs(CaseData caseData) {
        List<CaseworkerCICDocument> finalDecisionDocs = new ArrayList<>();
        if (null != caseData.getCaseIssueFinalDecision()) {
            if (null != caseData.getCaseIssueFinalDecision().getDocument()
                && null != caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink()) {
                Document finalDecisionDocument = caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink();
                DocumentType documentCategory = (null != finalDecisionDocument.getCategoryId())
                    ? DocumentType.fromCategory(finalDecisionDocument.getCategoryId()).get()
                    : DocumentType.TRIBUNAL_DIRECTION;
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentEmailContent(caseData.getCaseIssueFinalDecision().getDocument().getDocumentEmailContent())
                    .documentLink(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())
                    .documentCategory(documentCategory)
                    .build();
                finalDecisionDocs.add(doc);
            }

            if (null != caseData.getCaseIssueFinalDecision().getFinalDecisionDraft()
                && !ObjectUtils.isEmpty(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft().getFilename())) {
                Document finalDecisionDocument = caseData.getCaseIssueFinalDecision().getFinalDecisionDraft();
                DocumentType documentCategory = (null != finalDecisionDocument.getCategoryId())
                    ? DocumentType.fromCategory(finalDecisionDocument.getCategoryId()).get()
                    : DocumentType.TRIBUNAL_DIRECTION;
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentLink(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft())
                    .documentCategory(documentCategory)
                    .build();
                finalDecisionDocs.add(doc);
            }
        }
        return finalDecisionDocs;
    }

    public static void removeFinalDecisionDoc(CaseData caseData, CaseData oldData) {
        List<ListValue<CaseworkerCICDocument>> wholeFinalDecisionDocList = DocumentListUtil.getAllFinalDecisionDocuments(oldData);

        if (wholeFinalDecisionDocList.size() > caseData.getCicCase().getFinalDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : wholeFinalDecisionDocList) {
                checkFinalDecision(caseData, cicDocumentListValue);
            }
        }
    }

    public static void removeDecisionDoc(CaseData caseData, CaseData oldData) {
        List<ListValue<CaseworkerCICDocument>> wholeDecisionDocList = DocumentListUtil.getAllDecisionDocuments(oldData);

        if (wholeDecisionDocList.size() > caseData.getCicCase().getDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> doc : wholeDecisionDocList) {
                if (!caseData.getCicCase().getDecisionDocumentList().contains(doc)
                    && doc.getValue().getDocumentLink().equals(caseData.getCaseIssueDecision().getIssueDecisionDraft())) {
                    caseData.getCaseIssueDecision().setIssueDecisionDraft(null);
                } else if (doc.getValue().getDocumentLink()
                    .equals(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())) {
                    caseData.getCaseIssueDecision().setDecisionDocument(EMPTY_DOCUMENT);
                }
                addToRemovedDocuments(caseData.getCicCase(), doc.getValue());
            }
        }
    }

    public static void checkFinalDecision(CaseData caseData, ListValue<CaseworkerCICDocument> cicDocumentListValue) {
        if (!caseData.getCicCase().getFinalDecisionDocumentList().contains(cicDocumentListValue)) {
            if (cicDocumentListValue.getValue().getDocumentLink()
                .equals(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft())) {
                caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(null);
            } else if (cicDocumentListValue.getValue().getDocumentLink()
                .equals(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())) {
                caseData.getCaseIssueFinalDecision().setDocument(EMPTY_DOCUMENT);
            }
            addToRemovedDocuments(caseData.getCicCase(), cicDocumentListValue.getValue());
        }

    }

    public static void updateDecisionTypeDocumentList(CaseData caseData, CaseworkerCICDocument selectedDocument) {
        CaseIssueDecision caseIssueDecision = caseData.getCaseIssueDecision();
        if (null != caseIssueDecision.getDecisionDocument()
            && null != caseIssueDecision.getDecisionDocument().getDocumentLink()
            && selectedDocument.getDocumentLink().getUrl().equals(caseIssueDecision.getDecisionDocument().getDocumentLink().getUrl())) {
            caseIssueDecision.getDecisionDocument().getDocumentLink().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
            caseIssueDecision.getDecisionDocument().setDocumentEmailContent(selectedDocument.getDocumentEmailContent());
        } else if (null != caseIssueDecision.getIssueDecisionDraft()
            && selectedDocument.getDocumentLink().getUrl().equals(caseIssueDecision.getIssueDecisionDraft().getUrl())) {
            caseIssueDecision.getIssueDecisionDraft().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
        }
    }

    public static void updateFinalDecisionTypeDocumentList(CaseData caseData, CaseworkerCICDocument selectedDocument) {
        CaseIssueFinalDecision caseIssueFinalDecision = caseData.getCaseIssueFinalDecision();
        if (null != caseIssueFinalDecision.getDocument()
            && null != caseIssueFinalDecision.getDocument().getDocumentLink()
            && selectedDocument.getDocumentLink().getUrl().equals(caseIssueFinalDecision.getDocument().getDocumentLink().getUrl())) {
            caseIssueFinalDecision.getDocument().getDocumentLink().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
            caseIssueFinalDecision.getDocument().setDocumentEmailContent(selectedDocument.getDocumentEmailContent());
        } else if (null != caseIssueFinalDecision.getFinalDecisionDraft()
            && selectedDocument.getDocumentLink().getUrl().equals(caseIssueFinalDecision.getFinalDecisionDraft().getUrl())) {
            caseIssueFinalDecision.getFinalDecisionDraft().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
        }
    }

}
