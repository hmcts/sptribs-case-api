package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
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
        if (caseData.getCaseIssueDecision() != null) {
            if (caseData.getCaseIssueDecision().getDecisionDocument() != null
                && caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink() != null) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentEmailContent(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentEmailContent())
                    .documentLink(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                decisionDocs.add(doc);
            }

            if (caseData.getCaseIssueDecision().getIssueDecisionDraft() != null
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

    public static List<CaseworkerCICDocument> getFinalDecisionDocs(CaseData caseData) {
        List<CaseworkerCICDocument> finalDecisionDocs = new ArrayList<>();
        if (caseData.getCaseIssueFinalDecision() != null) {
            if (caseData.getCaseIssueFinalDecision().getDocument() != null
                && caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink() != null) {
                CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                    .documentEmailContent(caseData.getCaseIssueFinalDecision().getDocument().getDocumentEmailContent())
                    .documentLink(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())
                    .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                    .build();
                finalDecisionDocs.add(doc);
            }

            if (caseData.getCaseIssueFinalDecision().getFinalDecisionDraft() != null
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

    public static void addFinalDecisionDocumentsForRemoval(CaseData caseData, CaseData oldData) {
        List<ListValue<CaseworkerCICDocument>> wholeFinalDecisionDocList = DocumentListUtil.getAllFinalDecisionDocuments(oldData);

        if (wholeFinalDecisionDocList.size() > caseData.getCicCase().getFinalDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : wholeFinalDecisionDocList) {
                if (caseData.getCicCase().getFinalDecisionDocumentList().isEmpty()
                    || !caseData.getCicCase().getFinalDecisionDocumentList().contains(cicDocumentListValue)) {
                    addToRemovedDocuments(caseData.getCicCase(), cicDocumentListValue.getValue());
                }
            }
        }
    }

    public static void addDecisionDocumentsForRemoval(CaseData caseData, CaseData oldData) {
        List<ListValue<CaseworkerCICDocument>> previousDecisionDocList = DocumentListUtil.getAllDecisionDocuments(oldData);

        if (previousDecisionDocList.size() > caseData.getCicCase().getDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> doc : previousDecisionDocList) {
                if (caseData.getCicCase().getDecisionDocumentList().isEmpty()
                        || !caseData.getCicCase().getDecisionDocumentList().contains(doc)) {
                    addToRemovedDocuments(caseData.getCicCase(), doc.getValue());
                }
            }
        }
    }

    public static void removeFinalDecisionDraftAndCICDocument(CaseData caseData, ListValue<CaseworkerCICDocument> cicDocumentListValue) {
        if (caseData.getCaseIssueFinalDecision() != null) {
            if (caseData.getCaseIssueFinalDecision().getFinalDecisionDraft() != null
                && cicDocumentListValue.getValue().getDocumentLink()
                    .equals(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft())) {
                caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(null);
            } else if (caseData.getCaseIssueFinalDecision().getDocument() != null
                && cicDocumentListValue.getValue().getDocumentLink()
                    .equals(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())) {
                caseData.getCaseIssueFinalDecision().setDocument(EMPTY_DOCUMENT);
            }
        }
    }

    public static void removeDecisionDraftAndCICDocument(CaseData caseData, ListValue<CaseworkerCICDocument> cicDocumentListValue) {
        if (caseData.getCaseIssueDecision() != null) {
            if (caseData.getCaseIssueDecision().getIssueDecisionDraft() != null
                && cicDocumentListValue.getValue().getDocumentLink()
                    .equals(caseData.getCaseIssueDecision().getIssueDecisionDraft())) {
                caseData.getCaseIssueDecision().setIssueDecisionDraft(null);
            } else if (caseData.getCaseIssueDecision().getDecisionDocument() != null
                && cicDocumentListValue.getValue().getDocumentLink()
                    .equals(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())) {
                caseData.getCaseIssueDecision().setDecisionDocument(EMPTY_DOCUMENT);
            }
        }
    }
}
