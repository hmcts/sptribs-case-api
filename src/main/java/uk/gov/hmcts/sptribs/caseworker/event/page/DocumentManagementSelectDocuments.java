package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.DocumentConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.buildListValues;

public class DocumentManagementSelectDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectCaseDocuments", this::midEvent)
            .pageLabel("Select documents")
            .label("LabelSelectCaseDocuments", "")
            .label("LabelSelectCaseDocumentsWarning", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getAmendDocumentList)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        setSelectedDocuments(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private void setSelectedDocuments(CaseData data) {
        var cicCase = data.getCicCase();
        DynamicList documentList = cicCase.getAmendDocumentList();
        List<CaseworkerCICDocument> allCaseDocumentsList = DocumentListUtil.getAllCaseDocuments(data);
        List<ListValue<CaseworkerCICDocument>> allCaseDocuments = buildListValues(allCaseDocumentsList);
        CaseworkerCICDocument selectedDocument = null;
        String selectedDocumentType = null;

        if (!ObjectUtils.isEmpty(documentList.getValue())) {
            String selectedDocumentURL = documentList.getValue().getLabel();

            for (ListValue<CaseworkerCICDocument> documentListValue : allCaseDocuments) {
                String[] labels = selectedDocumentURL.split("--");
                String url = documentListValue.getValue().getDocumentLink().getUrl();
                if (ArrayUtils.isNotEmpty(labels) && labels[2].equals(url)) {
                    selectedDocument = documentListValue.getValue();
                    selectedDocumentType = labels[0];
                }
            }

            cicCase.setSelectedDocument(selectedDocument);
            cicCase.setSelectedDocumentType(selectedDocumentType);
            cicCase.setIsDocumentCreatedFromTemplate(isDocumentCreatedFromTemplate(data, selectedDocument, selectedDocumentType));
        }
    }

    private YesOrNo isDocumentCreatedFromTemplate(CaseData caseData, CaseworkerCICDocument selectedDocument, String selectedDocumentType) {
        YesOrNo isDocumentCreatedFromTemplate = YesOrNo.NO;
        switch (selectedDocumentType) {
            case DocumentConstants.DECISION_TYPE:
                if (null != caseData.getCaseIssueDecision().getIssueDecisionDraft()
                    && !ObjectUtils.isEmpty(caseData.getCaseIssueDecision().getIssueDecisionDraft().getFilename())
                    && caseData.getCaseIssueDecision().getIssueDecisionDraft().getUrl().equals(selectedDocument.getDocumentLink().getUrl())) {
                    isDocumentCreatedFromTemplate = YesOrNo.YES;
                }
                break;
            case DocumentConstants.FINAL_DECISION_TYPE:
                if (null != caseData.getCaseIssueFinalDecision().getFinalDecisionDraft()
                    && !ObjectUtils.isEmpty(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft().getFilename())
                    && caseData.getCaseIssueFinalDecision().getFinalDecisionDraft().getUrl().equals(selectedDocument.getDocumentLink().getUrl())) {
                    isDocumentCreatedFromTemplate = YesOrNo.YES;
                }
                break;
            case DocumentConstants.ORDER_TYPE:
                if (!CollectionUtils.isEmpty(caseData.getCicCase().getOrderList())) {
                    Optional<ListValue<Order>> orderListValue =
                        caseData.getCicCase().getOrderList()
                            .stream().filter(e -> null != e.getValue().getDraftOrder()
                                && e.getValue().getDraftOrder().getTemplateGeneratedDocument().getUrl()
                                .equals(selectedDocument.getDocumentLink().getUrl())).findFirst();
                    if (orderListValue.isPresent()) {
                        ListValue<Order> orderValue = orderListValue.get();
                        if (null != orderValue.getValue().getDraftOrder().getTemplateGeneratedDocument()
                            && !ObjectUtils.isEmpty(orderValue.getValue().getDraftOrder().getTemplateGeneratedDocument().getFilename())) {
                            isDocumentCreatedFromTemplate = YesOrNo.YES;
                        }
                    }

                }
                break;
        }

        return isDocumentCreatedFromTemplate;
    }

}
