package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

public class DocumentManagementSelectDocuments implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseAmendDocumentList=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectCaseDocuments", this::midEvent)
            .pageLabel("Select documents")
            .label("LabelSelectCaseDocuments", "")
            .label("LabelSelectCaseDocumentsWarning", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getAmendDocumentList)
            .readonly(CicCase::getAllDocumentList, ALWAYS_HIDE)
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
        List<ListValue<CaseworkerCICDocument>> allCaseDocuments = data.getCicCase().getAllDocumentList();
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
        }
    }

}
