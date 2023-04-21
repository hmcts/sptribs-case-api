package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

public class SelectCaseDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectCaseDocuments", this::midEvent)
            .pageLabel("Select documentation")
            .label("LabelSelectCaseDocuments", "")
            .complex(CaseData::getDocManagement)
            .mandatory(DocumentManagement::getDocumentsToAmend)
            .mandatory(DocumentManagement::getTribunalDocuments, "documentsToAmendCONTAINS  \"Tribunal form\"",
                "", "Tribunal form")
            .mandatory(DocumentManagement::getApplicationEvidences, "documentsToAmendCONTAINS  \"Applicant evidence\"",
                "", "Applicant evidence")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        var caseData = details.getData();
        var documentManagement = caseData.getDocManagement();

        List<ListValue<CaseworkerCICDocument>> documentList = DocumentListUtil.prepareSelectedDocumentList(caseData, true,
            documentManagement.getTribunalDocuments(), documentManagement.getApplicationEvidences());
        documentManagement.setDocumentList(documentList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
