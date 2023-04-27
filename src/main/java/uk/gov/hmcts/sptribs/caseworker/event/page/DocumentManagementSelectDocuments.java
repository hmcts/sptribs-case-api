package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentManagementSelectDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectCaseDocuments")
            .pageLabel("Select documents")
            .label("LabelSelectCaseDocuments", "")
            .label("LabelSelectCaseDocumentsWarning", "")
            .complex(CaseData::getDocManagement)
            .optional(DocumentManagement::getDocumentList)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        updateSelectedDocuments(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private void updateSelectedDocuments(CaseData data) {
        DynamicMultiSelectList documentList = data.getDocManagement().getDocumentList();
        if (!ObjectUtils.isEmpty(documentList.getValue()) && documentList.getValue().size() > 0) {
            List<DynamicListElement> documents = documentList.getValue();
            List<String> selectedDocumentUUIds = documents.stream().map(e -> e.getCode().toString()).collect(Collectors.toList());
            List<ListValue<CaseworkerCICDocument>> caseDocuments = data.getDocManagement().getCaseworkerCICDocument();

            List<ListValue<CaseworkerCICDocument>> selectedDocumentList =
                getSelectedCaseworkerCICDocumentList(caseDocuments, selectedDocumentUUIds);
            data.getDocManagement().setSelectedDocuments(selectedDocumentList);

        }
    }

    private List<ListValue<CaseworkerCICDocument>> getSelectedCaseworkerCICDocumentList(
        List<ListValue<CaseworkerCICDocument>> allCaseDocuments,
        List<String> selectedDocumentUUIds) {

        List<ListValue<CaseworkerCICDocument>> selectedDocumentList = new ArrayList<>();

        for (ListValue<CaseworkerCICDocument> documentListValue : allCaseDocuments) {
            String binaryUrl = documentListValue.getValue().getDocumentLink().getBinaryUrl();
            if (selectedDocumentUUIds.contains(StringUtils.substringAfterLast(binaryUrl, "/"))) {
                selectedDocumentList.add(documentListValue);
            }
        }

        return selectedDocumentList;
    }
}
