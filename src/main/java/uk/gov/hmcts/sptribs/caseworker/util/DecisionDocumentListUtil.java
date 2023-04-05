package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

public  final class DecisionDocumentListUtil {

    private DecisionDocumentListUtil() {

    }


    public static List<CaseworkerCICDocument> getDecisionDocs(CaseData caseData) {
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

    public static List<CaseworkerCICDocument> getFinalDecisionDocs(CaseData caseData) {
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

}
