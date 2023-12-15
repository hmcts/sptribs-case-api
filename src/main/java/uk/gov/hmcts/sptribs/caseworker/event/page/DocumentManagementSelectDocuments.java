package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;

public class DocumentManagementSelectDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectCaseDocuments", this::midEvent)
            .pageLabel("Select documents")
            .label("LabelSelectCaseDocuments", "")
            .label("LabelSelectCaseDocumentsWarning", "")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getAmendDocumentList)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData data = details.getData();
        setSelectedDocument(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private void setSelectedDocument(CaseData data) {
        final CicCase cicCase = data.getCicCase();
        final DynamicList documentList = cicCase.getAmendDocumentList();
        final String selectedDocumentLabel = documentList.getValue().getLabel();

        Optional<CaseworkerCICDocument> selectedDocument =
            Stream.ofNullable(DocumentListUtil.getAllCaseDocuments(data))
                .flatMap(Collection::stream)
                .map(ListValue::getValue)
                .filter(document -> selectedDocumentLabel.contains(document.getDocumentLink().getFilename()))
                .findFirst();

        selectedDocument.ifPresent(cicCase::setSelectedDocument);
        cicCase.setSelectedDocumentType(selectedDocumentLabel.split(DOUBLE_HYPHEN)[0]);
    }
}
