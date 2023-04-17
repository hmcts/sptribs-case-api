package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class IssueCaseAdditionalDocument implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectAdditionalDocument", this::midEvent)
            .pageLabel("Select documentation")
            .label("LabelSelectAdditionalDocument", "")
            .complex(CaseData::getCaseIssue)
            .mandatory(CaseIssue::getAdditionalDocument)
            .mandatory(CaseIssue::getTribunalDocuments, "issueCaseAdditionalDocumentCONTAINS  \"Tribunal form\"")
            .mandatory(CaseIssue::getApplicationEvidences, "issueCaseAdditionalDocumentCONTAINS  \"Applicant evidence\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        var caseData = details.getData();

        DynamicMultiSelectList documentList = DocumentListUtil.prepareDocumentList(caseData, true);
        caseData.getCaseIssue().setDocumentList(documentList);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
