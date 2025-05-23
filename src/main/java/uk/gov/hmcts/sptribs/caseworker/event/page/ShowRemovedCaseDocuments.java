package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class ShowRemovedCaseDocuments implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showRemovedCaseDocuments")
            .pageLabel("Removed documents")
            .label("LabelShowRemovedCaseDocuments", "")
            .label("LabelShowRemovedCaseDocumentsWarning", "Below documents will be removed")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getSelectRemoveDocumentList, "Select which documents to remove")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        DynamicMultiSelectList list = data.getCicCase().getSelectRemoveDocumentList();
        if (list != null && list.getValue() != null && list.getValue().isEmpty()) {
            errors.add("Please remove at least one document to continue");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(errors)
                .build();
    }

}
