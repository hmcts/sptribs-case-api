package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ShowCaseDocuments implements CcdPageConfiguration {

    private final CICDocument emptyDocument = new CICDocument();

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showCaseDocuments", this::midEvent)
            .pageLabel("Show case documents")
            .label("LabelShowCaseDocuments", "")
            .complex(CaseData::getDocManagement)
            .readonlyWithLabel(DocumentManagement::getCaseworkerCICDocument, "Document Management Files")
            .done()
            .complex(CaseData::getCloseCase)
            .readonly(CloseCase::getDocuments)
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReinstateDocuments)
            .readonly(CicCase::getApplicantDocumentsUploaded)
            .readonly(CicCase::getOrderDocumentList)
            .readonly(CicCase::getFinalDecisionDocumentList)
            .readonly(CicCase::getDecisionDocumentList)
            .done()
            .complex(CaseData::getListing)
            .complex(Listing::getSummary)
            .readonlyWithLabel(HearingSummary::getRecFile, "Hearing Summary Documents")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CaseData oldData = detailsBefore.getData();
        if (ObjectUtils.isEmpty(data.getCicCase().getRemovedDocumentList())) {
            List<ListValue<CaseworkerCICDocument>> removedDocumentList = new ArrayList<>();
            data.getCicCase().setRemovedDocumentList(removedDocumentList);
        }
        removeEvaluatedListDoc(data, oldData);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private void removeEvaluatedListDoc(CaseData caseData, CaseData oldData) {
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
    }

    private void removeFinalDecisionDoc(CaseData caseData, CaseData oldData) {
        List<ListValue<CaseworkerCICDocument>> wholeFinalDecisionDocList = DocumentListUtil.getAllFinalDecisionDocuments(oldData);

        if (wholeFinalDecisionDocList.size() > caseData.getCicCase().getFinalDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : wholeFinalDecisionDocList) {
                checkFinalDecision(caseData, cicDocumentListValue);
            }
        }
    }

    private void removeDecisionDoc(CaseData caseData, CaseData oldData) {
        List<ListValue<CaseworkerCICDocument>> wholeDecisionDocList = DocumentListUtil.getAllDecisionDocuments(oldData);

        if (wholeDecisionDocList.size() > caseData.getCicCase().getDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> doc : wholeDecisionDocList) {
                if (!caseData.getCicCase().getDecisionDocumentList().contains(doc)
                    && doc.getValue().getDocumentLink().equals(caseData.getCaseIssueDecision().getIssueDecisionDraft())) {
                    caseData.getCaseIssueDecision().setIssueDecisionDraft(null);
                } else if (doc.getValue().getDocumentLink()
                    .equals(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())) {
                    caseData.getCaseIssueDecision().setDecisionDocument(emptyDocument);
                }
                addToRemovedDocuments(caseData.getCicCase(), doc.getValue());
            }
        }
    }

    private void checkFinalDecision(CaseData caseData, ListValue<CaseworkerCICDocument> cicDocumentListValue) {
        if (!caseData.getCicCase().getFinalDecisionDocumentList().contains(cicDocumentListValue)) {
            if (cicDocumentListValue.getValue().getDocumentLink()
                .equals(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft())) {
                caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(null);
            } else if (cicDocumentListValue.getValue().getDocumentLink()
                .equals(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())) {
                caseData.getCaseIssueFinalDecision().setDocument(emptyDocument);
            }
            addToRemovedDocuments(caseData.getCicCase(), cicDocumentListValue.getValue());
        }

    }

    private CicCase removeOrderDoc(CicCase cicCase, CicCase oldCicCase) {
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

    private void manageUploadedFiles(ListValue<Order> orderListValue, ListValue<CaseworkerCICDocument> cicDocumentListValue) {
        if (!CollectionUtils.isEmpty(orderListValue.getValue().getUploadedFile())) {
            for (int i = 0; i < orderListValue.getValue().getUploadedFile().size(); i++) {
                ListValue<CICDocument> file = orderListValue.getValue().getUploadedFile().get(i);
                if (null != file.getValue().getDocumentLink() && file.getValue().getDocumentLink()
                    .equals(cicDocumentListValue.getValue().getDocumentLink())) {
                    orderListValue.getValue().getUploadedFile().get(i).setValue(emptyDocument);

                }
            }
        }
    }

    private void checkLists(CaseData caseData, List<ListValue<CaseworkerCICDocument>> oldList,
                            List<ListValue<CaseworkerCICDocument>> updatedList) {
        if (CollectionUtils.isEmpty(updatedList)) {
            for (ListValue<CaseworkerCICDocument> doc : oldList) {
                addToRemovedDocuments(caseData.getCicCase(), doc.getValue());
            }
        } else {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : oldList) {
                boolean found = false;
                for (ListValue<CaseworkerCICDocument> doc : updatedList) {
                    if (cicDocumentListValue.getValue().equals(doc.getValue())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    addToRemovedDocuments(caseData.getCicCase(), cicDocumentListValue.getValue());
                }
            }
        }

    }

    private void addToRemovedDocuments(CicCase cicCase, CaseworkerCICDocument caseworkerCICDocument) {
        if (CollectionUtils.isEmpty(cicCase.getRemovedDocumentList())) {
            List<ListValue<CaseworkerCICDocument>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<CaseworkerCICDocument>builder()
                .id("1")
                .value(caseworkerCICDocument)
                .build();

            listValues.add(listValue);

            cicCase.setRemovedDocumentList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<CaseworkerCICDocument>builder()
                .value(caseworkerCICDocument)
                .build();

            cicCase.getRemovedDocumentList().add(0, listValue); // always add new note as first element so that it is displayed on top

            cicCase.getRemovedDocumentList().forEach(
                removedFileListValue -> removedFileListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
    }
}
