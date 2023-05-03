package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.CaseDocumentListUtil.updateCaseDocumentList;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.updateDecisionTypeDocumentList;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.updateFinalDecisionTypeDocumentList;
import static uk.gov.hmcts.sptribs.caseworker.util.OrderDocumentListUtil.updateOrderTypeDocumentList;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CLOSE_CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DECISION_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOC_MGMT_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.HEARING_SUMMARY_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.ORDER_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.REINSTATE_TYPE;

public class DocumentManagementAmendDocuments implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseAmendDocumentList=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("amendCaseDocuments", this::midEvent)
            .pageLabel("Amend case documents")
            .label("LabelAmendCaseDocuments", "")
            .label("LabelAmendCaseDocumentsMessage", "Amend the document details below")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getAllDocumentList, ALWAYS_HIDE)
            .readonly(CicCase::getSelectedDocumentType, ALWAYS_HIDE)
            .complex(CicCase::getSelectedDocument)
            .mandatory(CaseworkerCICDocument::getDocumentCategory)
            .mandatory(CaseworkerCICDocument::getDocumentEmailContent)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        var cicCase = data.getCicCase();
        final List<String> errors = new ArrayList<>();

        CaseworkerCICDocument selectedDocument = cicCase.getSelectedDocument();
        String selectedDocumentType = cicCase.getSelectedDocumentType();

        switch (selectedDocumentType) {
            case ORDER_TYPE:
                updateOrderTypeDocumentList(cicCase, selectedDocument);
                break;
            case CASE_TYPE:
                updateCaseDocumentList(cicCase.getApplicantDocumentsUploaded(), selectedDocument);
                break;
            case REINSTATE_TYPE:
                updateCaseDocumentList(cicCase.getReinstateDocuments(), selectedDocument);
                break;
            case DECISION_TYPE:
                updateDecisionTypeDocumentList(data, selectedDocument);
                break;
            case FINAL_DECISION_TYPE:
                updateFinalDecisionTypeDocumentList(data, selectedDocument);
                break;
            case DOC_MGMT_TYPE:
                updateCaseDocumentList(data.getAllDocManagement().getCaseworkerCICDocument(), selectedDocument);
                break;
            case CLOSE_CASE_TYPE:
                updateCaseDocumentList(data.getCloseCase().getDocuments(), selectedDocument);
                break;
            case HEARING_SUMMARY_TYPE:
                updateCaseDocumentList(data.getListing().getSummary().getRecFile(), selectedDocument);
                break;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }



}
